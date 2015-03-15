Temporal Random Indexing (v. 0.20 - 15/03/2015)
==========================================================================

General info
------------

This software provides a framework for the Temporal Random Indexing (TRI) technique. TRI is able to build WordSpaces taking into account temporal information. A WordSpace is a geometrical space in which words are represented as mathematical points. Similar words are represented close in the WordSpace. TRI can build different WordSpaces for several time periods allowing the analysis of how words change their meaning over time.

The WordSpaces are built using a corpus of documents annotated with the year of publication.

Details about the algorithm are published in the following paper:

@inproceedings{clic2014a,<br>
	title            = "Analysing Word Meaning over Time by Exploiting Temporal Random Indexing",<br>
	year             = "2014",<br>
	author           = "Pierpaolo Basile and Annalina Caputo and Giovanni Semeraro",<br>
	booktitle        = "First Italian Conference on Computational Linguistics CLiC-it 2014",<br>
	editor           = "Roberto Basili and Alessandro Lenci and Bernardo Magnini",<br>
	publisher        = "Pisa University Press",<br>
	url              = "http://clic.humnet.unipi.it/proceedings/Proceedings-CLICit-2014.pdf"}<br>

Please, cite the paper if you adopt our tool.

Usage
-----

The TRI framework provides both tools for building WordSpaces and shell to query WordSpaces performing linguistic analysis.

Prepare the corpus:

1.	Create a directory for the corpus
2.	Copy all documents in the directory, each document must contain the information about the publication year in this format: filename\_year, for example *myfile\_1981*
3.	Run the class **di.uniba.it.tri.occ.BuildOccurrence** with the parameters: *corpusDir* *outputOccDir* *windowSize* *extractor\_class* *regular expression used to filter filenames*. The *corpusDir* is the corpus directory (step 1), *outputOccDir* is the directory in which information about co-occurrences will be stored. The *extractor\_class* is the name of the class used to extract text from files. This class must implement the interface: *di.uniba.it.tri.extractor.Extractor*. Three extractors are implemented: *GutenbergExtractor* for the Gutenberg Project; *AANExtractor* for the AAN corpus,; and *TxtExtractor* for plain text file.

Build the WordSpaces:
1.	Run the class **di.uniba.it.tri.space.SpaceBuilder** with the arguments: *outputOccDir* *outputSpaceDir* *dimension* *seed* *dictionarySize*. The *outputOccDir* is the directory of co-occurrences, *outputSpaceDir* is the directory in which WordSpaces will be stored, *dimension* and *seed* are Random Indexing parameters (1000 and 20 are good values), and the dictionarySize is the number of terms considered into the vocabulary (the most frequent terms are considered).


After these steps the *outputSpaceDir* contains a WordSpace for each year in the corpus. Now you can use the TRI shell to analyze the corpus running the class: **di.uniba.it.tri.shell.TriShell**. If you note some problems related to characters encoding you can run the shell passing as argument the charset. The default charset is ISO-8859-1. Type **help** for shell usage, **help \*** to show the commands list and **help command>** to visualize info about a specific command.

Command line help
-----------------

**di.uniba.it.tri.shell.TriShell**

usage: Run the TRI shell<br>
 -c <arg>   The charset used by the shell (optional)

**di.uniba.it.tri.occ.BuildOccurrence**

usage: Build the co-occurrences matrix given the set of files with year
       metadata [-c <arg>] [-e <arg>] [-o <arg>] [-r <arg>] [-w <arg>]<br>
 -c <arg>   The corpus directory containing files with year metadata<br>
 -e <arg>   The class used to extract the content from files<br>
 -o <arg>   Output directory where output will be stored<br>
 -r <arg>   Regular expression used to fetch files (optional, default ".+")<br>
 -w <arg>   The window size used to compute the co-occurrences

**di.uniba.it.tri.space.SpaceBuilder**

usage: Build WordSpace using Temporal Random Indexing [-c <arg>] [-d
       <arg>] [-o <arg>] [-s <arg>] [-v <arg>]<br>
 -c <arg>   The directory containing the co-occurrences matrices<br>
 -d <arg>   The vector dimension (optional, defaults 300)<br>
 -o <arg>   Output directory where WordSpaces will be stored<br>
 -s <arg>   The number of seeds (optional, defaults 10)<br>
 -v <arg>   The dictionary size (optional, defaults 100000)

Contacts
--------
Pierpaolo Basile, pierpaolo.basile@gmail.com.
