package com.homeshop.seebazar.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

/**
 * One chat document per buyer–vendor pair; messages in subcollection [SUB_MESSAGES].
 */
object ChatFirestore {
    const val COL_CHATS = "chats"
    const val SUB_MESSAGES = "messages"

    private const val FIELD_PARTICIPANTS = "participants"
    private const val FIELD_VENDOR_UID = "vendorUid"
    private const val FIELD_BUYER_UID = "buyerUid"
    private const val FIELD_VENDOR_LABEL = "vendorLabel"
    private const val FIELD_BUYER_LABEL = "buyerLabel"
    private const val FIELD_LAST_MESSAGE = "lastMessage"
    private const val FIELD_LAST_AT = "lastAtMillis"
    private const val FIELD_TEXT = "text"
    private const val FIELD_FROM_UID = "fromUid"
    private const val FIELD_CREATED_AT = "createdAtMillis"

    fun roomId(vendorUid: String, buyerUid: String): String =
        if (vendorUid < buyerUid) "${vendorUid}__${buyerUid}" else "${buyerUid}__${vendorUid}"

    fun chatsCollection() = FirebaseFirestore.getInstance().collection(COL_CHATS)

    /**
     * Creates or merges room metadata (does not delete messages). Loads display names from user docs when blank.
     */
    fun ensureChatRoom(
        vendorUid: String,
        buyerUid: String,
        vendorLabelHint: String,
        buyerLabelHint: String,
        onDone: (roomId: String?, Throwable?) -> Unit,
    ) {
        if (vendorUid.isBlank() || buyerUid.isBlank()) {
            onDone(null, IllegalArgumentException("Missing participant"))
            return
        }
        val room = roomId(vendorUid, buyerUid)
        val roomRef = chatsCollection().document(room)
        val db = FirebaseFirestore.getInstance()
        val vRef = UserFirestore.usersCollection().document(vendorUid)
        val bRef = UserFirestore.usersCollection().document(buyerUid)
        vRef.get().addOnCompleteListener { vt ->
            val vName = vt.result?.getString(UserFirestore.FIELD_NAME)?.trim().orEmpty()
                .ifBlank { vendorLabelHint }
            bRef.get().addOnCompleteListener { bt ->
                val bName = bt.result?.getString(UserFirestore.FIELD_NAME)?.trim().orEmpty()
                    .ifBlank { buyerLabelHint }
                val payload = hashMapOf<String, Any>(
                    FIELD_VENDOR_UID to vendorUid,
                    FIELD_BUYER_UID to buyerUid,
                    FIELD_PARTICIPANTS to listOf(vendorUid, buyerUid),
                    FIELD_VENDOR_LABEL to vName.ifBlank { "Vendor" },
                    FIELD_BUYER_LABEL to bName.ifBlank { "Customer" },
                    FIELD_LAST_MESSAGE to "",
                    FIELD_LAST_AT to 0L,
                )
                roomRef.set(payload, SetOptions.merge())
                    .addOnCompleteListener { t ->
                        if (t.isSuccessful) onDone(room, null) else onDone(null, t.exception)
                    }
            }
        }
    }

    fun sendMessage(
        roomId: String,
        fromUid: String,
        text: String,
        onDone: (Throwable?) -> Unit,
    ) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            onDone(null)
            return
        }
        val db = FirebaseFirestore.getInstance()
        val roomRef = chatsCollection().document(roomId)
        val msgRef = roomRef.collection(SUB_MESSAGES).document()
        val batch = db.batch()
        batch.set(
            msgRef,
            mapOf(
                FIELD_TEXT to trimmed,
                FIELD_FROM_UID to fromUid,
                FIELD_CREATED_AT to System.currentTimeMillis(),
            ),
        )
        batch.update(
            roomRef,
            mapOf(
                FIELD_LAST_MESSAGE to trimmed.take(200),
                FIELD_LAST_AT to System.currentTimeMillis(),
            ),
        )
        batch.commit().addOnCompleteListener { onDone(it.exception) }
    }

    fun listenMessages(
        roomId: String,
        onUpdate: (List<ChatMessage>) -> Unit,
    ): ListenerRegistration =
        chatsCollection().document(roomId).collection(SUB_MESSAGES)
            .orderBy(FIELD_CREATED_AT, Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val list = snap.documents.mapNotNull { d ->
                    val m = d.data ?: return@mapNotNull null
                    ChatMessage(
                        id = d.id,
                        text = m[FIELD_TEXT]?.toString().orEmpty(),
                        fromUid = m[FIELD_FROM_UID]?.toString().orEmpty(),
                        createdAtMillis = (m[FIELD_CREATED_AT] as? Number)?.toLong()
                            ?: m[FIELD_CREATED_AT]?.toString()?.toLongOrNull() ?: 0L,
                    )
                }
                onUpdate(list)
            }

    fun listenRoomsForUser(
        uid: String,
        onUpdate: (List<ChatRoomSummary>) -> Unit,
    ): ListenerRegistration =
        chatsCollection()
            .whereArrayContains(FIELD_PARTICIPANTS, uid)
            .addSnapshotListener { snap, _ ->
                if (snap == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val list = snap.documents.mapNotNull { d ->
                    val m = d.data ?: return@mapNotNull null
                    ChatRoomSummary(
                        roomId = d.id,
                        vendorUid = m[FIELD_VENDOR_UID]?.toString().orEmpty(),
                        buyerUid = m[FIELD_BUYER_UID]?.toString().orEmpty(),
                        vendorLabel = m[FIELD_VENDOR_LABEL]?.toString().orEmpty(),
                        buyerLabel = m[FIELD_BUYER_LABEL]?.toString().orEmpty(),
                        lastMessage = m[FIELD_LAST_MESSAGE]?.toString().orEmpty(),
                        lastAtMillis = (m[FIELD_LAST_AT] as? Number)?.toLong()
                            ?: m[FIELD_LAST_AT]?.toString()?.toLongOrNull() ?: 0L,
                    )
                }.sortedByDescending { it.lastAtMillis }
                onUpdate(list)
            }
}

data class ChatMessage(
    val id: String,
    val text: String,
    val fromUid: String,
    val createdAtMillis: Long,
)

data class ChatRoomSummary(
    val roomId: String,
    val vendorUid: String,
    val buyerUid: String,
    val vendorLabel: String,
    val buyerLabel: String,
    val lastMessage: String,
    val lastAtMillis: Long,
)
