Temporal Random Indexing (alpha version)
==========================================================================

General info
------------

This software provides a framework for the Temporal Random Indexing (TRI) technique. TRI is able to build WordSpaces taking into account temporal information. A WordSpace is a geometrical space in which words are represented as mathematical points. Similar words are represented close in the WordSpace. TRI can build different WordSpaces for several time periods allowing the analysis of how words change their meaning over time.

The WordSpaces are build using a corpus of documents annotated with the year of publication.
 
Details about the algorithm will be published in the following paper:

**(NOTE citation details are not yet available)**

Usage
-----

The TRI framework provides both tools for building WordSpaces and shell to query the WordSpaces and perform linguistic analysis.

Prepare the corpus:

1.	Create a directory for the corpus
2.	Copy all documents in the directory, each document must contain the information about the publication year in this format: filename\_year, for example myfile\_1981
3.	Run the class di.uniba.it.tri.occ.BuildOccurrence with the parameters: corpusDir outputOccDir windowSize extractor\_class. corpusDir is the corpus directory (step 1), outputOccDir is the directory in which information about co-occurrences will be stored. The extractor\_class is the full name (package+class name) of the class used to extract text from files. This class must implement the interface: di.uniba.it.tri.extractor.Extractor. An extractor for the Gutenberg Project is implemented by the class: di.uniba.it.tri.extractor.GutenbergExtractor.

Build the WordSpaces:

1.	Run the class di.uniba.it.tri.space.SpaceBuilder with the arguments: outputOccDir outputSpaceDir dimension seed dictionarySize. outputOccDir is the directory of co-occurrences, outputSpaceDir is the directory in which WordSpaces will be stored, dimension and seed are Random Indexing parameters (1000 and 20 are good values), and the dictionarySize is the number of terms considered into the vocabulary (the most frequent terms are considered).


After these steps the outputSpaceDir contains a WordSpace for each year in the corpus. Now you can use the TRI shell to analyze the corpus running the class: di.uniba.it.tri.shell.TriShell. If you note some problems related to characters encoding you can run the shell passing as argument the charset. The default charset is ISO-8859-1. Type help for shell usage, help * to show the commands list and help command_name to visualize info about a specific command.

Contacts
--------
Pierpaolo Basile, pierpaolo.basile@gmail.com.



