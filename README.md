# COMP3260_A2
A Java implementation of the AES encryption and decryption algorithms for ECB, CBC, CFB and OFB.

## Installation
Unpackage or clone into a directory

cd into directory and
```java
javac *.java
```

## Usage
Place all input data into a .txt file, and save it in the main project directory.

Example program run for input.txt
```java
java AESInteface input.txt
```

The program will prompt for output of ciphertext or plaintext.

This can be displayed to cli or output to file, in which case will be stored in output_input.txt

## Notes
It is assumed that all input data is accurate.

###Input format is as follows:

Line 1 - 0 or 1 for encryption or decryption respectively

Line 2 - AES mode, ECB, CFB, CBC or OFB as 0, 1, 2, 3 respectively

Line 3 - transmission size between 1 and 16 bytes or 0 if not applicable

Line 4-5 - 32 byte input text (plain or cipher) as hexadecimals.

Line 6 - 16 byte key as hexadecimals

Line 7 - 16 byte initialization vector as hexadecimals or 0 if not applicable

The program contains minimal validation so it is important that input matches this format

It is also important that the input file is placed in the main project directory.  Internal file paths are relative.


## Contributors
Ben Sutter - c3063467
Josh Howard - c3208014