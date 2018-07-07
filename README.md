# Japanese Helper

Semester project for the course "Industrial Strength Multilingual Language Analysis" at Universität Tübingen, Winter Semester 17-18. This application analyzes Japanese text and displays information about the text that helps English-speaking learners of Japanese, including word segmentation, translation, pronunciation, inflection information etc.

You can import it as a Maven project in Eclipse/IntelliJ IDEA and launch it as a GWT application, after having installed [the GWT plugin](http://gwt-plugins.github.io/documentation/gwt-eclipse-plugin/Download.html). Remember to check whether the resources folder is excluded from the Build Path. If so, include it before building and launching the project.

For detailed documentation (information about the project, its use cases, implementation, linguistic background, plus screenshots), please refer to [the PDF report](Report.pdf).

## Tokenizing files

- Add the (uncompressed) files to `src/main/webapp/WEB-INF/tokenize`. Make sure Eclipse notices the added files.
- If you want to tokenized content be separated by something other than a blank space, change the [`SEPARATOR` constant in `LookupServiceImpl.java`](https://github.com/ismla-japanese-helper/japanese-helper/blob/tokenize-files/src/main/java/de/ws1718/ismla/JapaneseHelper/server/LookupServiceImpl.java#L32).
- Run the project in SuperDev mode, open the website, and click the `Tokenize files` button. Update logs about which file is currently being read are in the console. Once all files have been processed, there is a pop-up on the website.
- The tokenized files are in the `target/JapaneseHelper-1.0-SNAPSHOT` directory. **Note that this directory is rebuilt and the files are deleted whenever the project is run again!**

## Licensed content

The Wikimedia Foundation licenses its texts on Wikipedia and Wiktionary under a [Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0)](https://creativecommons.org/licenses/by-sa/3.0/) license ([Here is the full text of the license.](https://creativecommons.org/licenses/by-sa/3.0/legalcode)). This applies to the files in our resource folders [```/src/main/webapp/WEB-INF/dictionary```](https://github.com/ismla-japanese-helper/japanese-helper/tree/master/src/main/webapp/WEB-INF/dictionary), [```/src/main/webapp/WEB-INF/difficulty-rating```](https://github.com/ismla-japanese-helper/japanese-helper/tree/master/src/main/webapp/WEB-INF/difficulty-rating), and [```/src/main/webapp/WEB-INF/inflection-templates```](https://github.com/ismla-japanese-helper/japanese-helper/tree/master/src/main/webapp/WEB-INF/inflection-templates), which are based on Wikipedia/Wiktionary articles. These files, including any modifications we made, are also licensed by the same license.
