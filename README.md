Android Secure Element (SE) access sample app
==============================================

Shows how to access the secure element on Android 4.0.4 and 4.1. 
Requires a rooted device to install and run. More details and 
background information in related blog posts: 

* http://nelenkov.blogspot.com/2012/08/accessing-embedded-secure-element-in.html
* http://nelenkov.blogspot.com/2012/08/android-secure-element-execution.html
* http://nelenkov.blogspot.com/2012/08/exploring-google-wallet-using-secure.html

**WARNING**

While this program doesn't try to modify the SE and doesn't contain any 
'dangerous' SE commands, using and/or modifying it may lock (brick) the secure 
element on your phone. Make sure you know what you are doing and use at your 
own risk!

Building
--------

1. Get the source code for the Java EMV Reader library from http://code.google.com/p/javaemvreader/ and build it.
2. Drop the resulting jar file in `lib/`.
3. Import the project in Eclipse and build it.

Installation and running
------------------------

1. Add the signing certificate to `/etc/nfcee_access.xml` on your device. 
This requires root access to remount `/system` as rw. See first blog post 
for details on file format. 
2. Sign and install the APK. 
3. Run from launcher or Eclipse. 
4. (Optional) Install Google Wallet to test EMV functionality. 


Read linked blog posts for more details. 



