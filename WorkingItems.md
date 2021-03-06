# TODO: A working tabulation of items that still need to get fixed for the next release of the library (TBD)

* Ability for users to create / specify icon types for custom file types (see ``FileChooserBuilder.DefaultFileTypes`` enum).
* Moving forward (especially with the restrictive upcoming Android 11 permissions settings), are going to need a way to
  translate an obtained chosen path, and then read from it without having to tag the returned results with
  FileProvider docIds that the user doesn't need to know about. Create a handy utils interface for this ???

* A working question of mine is why we need all of this abstraction just to access files on the local disks?
  I get that more abstract file types (e.g., from over the network) or interfaces can be desirable in complicated setups,
  but why when the stock Java ``File`` interfaces work already are we having to go through this convoluted way of
  confusing the user's access by standard file paths as IDs? The inner implementation suggested by Google just
  transforms the ``File`` back and forth from that object type to the document ID, or ``root:pathData`` format.
  1. NOTE: It is not always possible to set Posix file permissions on external storage in recent Android APIs.
  2. The system also does not like to always honor returning genuine properties back to the user about the document,
     even if it just corresponds to a (correct) path on disk obtained legitimately by the working ``DocumentsProvider` instance.
  3. In this case (just working off of the default file system on your Droid device), is the difference that the system
     gives some magic credence to interfaces derived from ``DocumentsProvider``? E.g., so that it will allow an
     instance of that interface to magically grok FS data and properties, but deny the same access to users that are only
     working within stock Java ``File`` I/O contexts?
  4. If (3) is true, I repeat my confusion and distaste for this scheme and added bits of convolution rather than
     useful abstraction to a generalized more powerful interface.
     I need more information, but the documentation for this is still very missing in a lot of places.

## Documentation and approach to handling files internally within the picker library

### Links to relevant Android API documentation

The next links provide some context and developer reference docs for the internal schemes used by the 
library to implement listing files on the local device file system. 
* [Android Storage Access Framework (SAF)](https://developer.android.com/guide/topics/providers/document-provider)
* [Accessing app-specific files (developer docs)](https://developer.android.com/training/data-storage/app-specific)
* [Access documents and other files from shared storage (developer docs)](https://developer.android.com/training/data-storage/shared/documents-files)
* [Data and file storage overview (developer docs)](https://developer.android.com/training/data-storage)
* [Overview of shared storage (developer docs)](https://developer.android.com/training/data-storage/shared)
* [Requesting a shared file (developer docs)](https://developer.android.com/training/secure-file-sharing/request-file.html)
* [MediaStore based file access(developer docs)](https://developer.android.com/reference/android/provider/MediaStore)
* [A tutorial with pointers](https://medium.com/androiddevelopers/building-a-documentsprovider-f7f2fb38e86a) 
   for the under documented new SAF and storage restrictions on recent Android releases
* [A detailed discussion of changed to Android storage policy](http://thewindowsupdate.com/2020/06/03/scoped-storage-in-android-10-android-11/)

### Listing of other file picker libraries for Android

We have also made use of some of the functionality provided in Kotlin and/or Java code from the 
following alternative Android file chooser libraries:
* [AndroidFilePicker library](https://github.com/rosuH/AndroidFilePicker)
* [NoNonsense-FilePicker library](https://github.com/spacecowboy/NoNonsense-FilePicker)
* [MultiType-FilePicker library](https://github.com/fishwjy/MultiType-FilePicker/blob/master/filepicker/src/main/java/com/vincent/filepicker/filter/callback/FileLoaderCallbacks.java)
* [LFilePicker library](https://github.com/leonHua/LFilePicker)
* [MaterialFilePicker library](https://github.com/nbsp-team/MaterialFilePicker)

Much of the reason for re-writing the Kotlin-based scheme in 
[my initial fork](https://github.com/maxieds/AndroidFilePicker) 
the first library above is 
centered on needing to re-theme and customize the UI, and also resolve some consistency issues 
when selecting only directory based paths. This library also deals with the timely issue of the 
new storage and access permissions that are popping up as Android devices being to migrate to 
SDK >= 11 (codename *Android* **Q**).

## Feature request for future bursts of coder energy

* The ``BasicFileProvider`` class has built-in functionality to query the top of the most recent 
  documents list. Perhaps this action would make a good naviagation folder alongside the 
  default folder paths to common directories displayed at the top of the chooser activity?
* My growing understanding of the new storage access changes on Android suggests that eventually 
  the ``String`` and ``File`` based returned data from the picker will not be easy to 
  turn into hooks that can actually open these obscured file paths. This means that even if you 
  have an absolute path to a local file, the Android system may not like to let you open the 
  file from that string reference under the new ways it limits storage access. 
  **Note to self** to eventually explore adding functionality to the picker to return 
  plaintext strings and byte arrays of the file contents in place of the path identifiers on disk. 
  This should require better usages of the ``GET_CONTENT_*_TREE_*`` intent actions, and 
  working with persistent URI data from the file provider:
  1. Modify ``openFile(...)`` to yield ``readFileAsString`` and/or ``readFileAsBytesArray`` 
     depending on the picker action the client code calls. 
  2. Note that returned ``MatrixCursor`` objects have columns that describe th file contents, and then 
     we can call ``getType(colIndex)`` to figure out whether the contents are: 
     string / plaintext data (like with ``FIELD_TYPE_STRING``) or stored as 
     binary data that can be read out in byte arrays (e.g., for `FIELD_TYPE_BLOB`).
