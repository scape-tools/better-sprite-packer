**Better Sprite Packer 3**

Is a tool designed to pack sprites in a way they can be random-accessed.

**Features**
* You can randomly access any sprite (extremely fast and less memory consumption especially for sprites that aren't used!)
* Images are fully preserved even the meta data, compression is done by the image format
* Transparency is never set, if you have transparent images they will remain transparent
* Displays image meta data
* Indexes are preserved (any image removed and if its not the last element will create a placeholder 0 byte)
* Import sprites from a directory
* Import from binary
* Select the sprites you want to add (with multi-selection)
* Export to raw formats
* Export to binary
* Search an image by it's imdex
* Modern, clean looking
* Made in Kotlin programming language
* Works on Windows, Mac, and Linux
* Supports PNG, JPEG, and GIF

**Info**

The programs exports your files in a binary format in 2 files
1. main_file_sprites.dat (this is where all of the image data is kept)
    * **format**
        * signature: "bsp" (3 bytes)
        * image: byte[] (variable length)
2. main_file_sprites.idx (this is the meta data that contains meta information)
    * **format**
        * dataOffset: 24uint
        * dataLength: 24uint
        * offsetX: ushort
        * offsetY: ushort

![image1](https://i.imgur.com/kNLk92s.png)

![image2](https://i.imgur.com/ADKSy2E.png)