Android Secure Element (SE) access sample code
==============================================

Shows how to access the secure element on Android 4.0.4 and 4.1. 
Requires a rooted device to install and run. More details and 
background information in related blog posts: 

http://nelenkov.blogspot.com/2012/08/accessing-embedded-secure-element-in.html
http://nelenkov.blogspot.com/2012/08/android-secure-element-execution.html
http://nelenkov.blogspot.com/2012/08/exploring-google-wallet-using-secure.html

Building
--------

1. Get the source code for the Java EMV Reader library and build it.
http://code.google.com/p/javaemvreader/
2. Drop the resulting jar file in lib/.
3. Import the project in Eclipse and build it.

Installation and running
------------------------

1. Add the signing certificate to `/etc/nfcee_access.xml` on your device. 
This requires root access to remount `system` as rw. See first blog post 
for details. 
2. Sign and install the APK. 
3. Run from launcher. 
4. (Optional) Install Google Wallet to test EMV functionality. 


Read blog posts for more details. 



