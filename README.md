# SeeBazar 🛍️

clone it : https://github.com/Sarim2022/seebazar-platform.git

database : firebase using for fast integration

SeeBazar is a **Jetpack Compose + Firebase** based multi-flow marketplace app where users can browse products, book service/reservation slots, place prepaid or postpaid orders, and vendors can manage products, bookings, and verify orders using QR scanning.

## ✨ Features

### User Side
- User registration & login
- Browse vendor products and services
- Add products to cart
- Book reservation slots
- Prepaid / Postpaid order flow
- Pickup time selection
- Order status screen
- Unique QR code generated for each order
- Live order status updates (`Pending` → `Done`)

### Vendor Side
- Vendor registration & login
- Vendor dashboard / home screen
- Manage products and services
- View incoming user orders
- View reservation bookings
- Vendor order status screen
- QR scanner to verify customer orders
- Mark orders as completed

## 🔥 Tech Stack
- **Kotlin**
- **Jetpack Compose**
- **Firebase Authentication**
- **Firebase Firestore**
- **QR Code Generation / Scanning**

## 📱 Order Flow
1. User adds product or reservation to cart  
2. Order is stored in Firestore  
3. User confirms order (Prepaid / Postpaid)  
4. Order appears in both **User** and **Vendor** order screens  
5. QR code is generated for the order  
6. Vendor scans QR at pickup  
7. Vendor marks order as **Done**  
8. Status updates on both sides  

## 🚀 Future Improvements
- Real UPI payment integration
- Push notifications
- Better analytics for vendors
- Search & filter
- Order history enhancements

## 📌 Status
This project is currently in active development and being built step-by-step.

---

**Built with ❤️ using Jetpack Compose**
