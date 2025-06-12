# ⚡Dex Loader (Android)

This project demonstrates different methods of loading and executing DEX files in Android, with a focus on dynamic loading techniques such as `DexClassLoader`, `PathClassLoader`, and in-memory execution.

---

## 📦 Project Structure

- **Dex Loader**: Loads and executes `contacts.dex`, which simulates reading device contacts.
- **PathClassLoader / In-Memory Dex Loader**: Load `classes.dex`, which contains a simple method that generates a random number and displays it on screen.
  - The **In-Memory Dex Loader** simulates fetching the dex file from a remote server before executing it.

---

## ⚠️ Android Version Limitations

> `DexClassLoader` usage is restricted on Android 10 and above. The demo may not function properly on newer devices due to scoped storage and runtime execution limitations.

---

## 📋 Permissions

The app requests the `READ_CONTACTS` permission because the `contacts.dex` file simulates accessing the user's contact list.

---

## 🚀 How It Works

1. `contacts.dex` is executed through DexClassLoader and prints contact data to **logcat**.
2. `classes.dex` is used in both PathClassLoader and In-Memory loaders to generate a random number, which is shown in the UI.
3. The In-Memory Dex Loader simulates downloading and executing a dex file directly from memory, without saving it to disk.

---

## 📌 Disclaimer

This project is intended for **educational and research purposes only**.  
**Do not use it for malicious, unethical, or illegal activities.**  
The author takes no responsibility for any misuse or damage caused.
