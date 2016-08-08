# Wikipedia-based Explicit Semantic Analysis

## What is this?

Semantic analysis is a way to extract meaning from a written text.
The written text may be a single word, a couple of words, a sentence, a paragraph or a whole book.

Explicit Semantic Analysis is a way to derive meaning based on Wikipedia.
The text is transformed into a vector of Wikipedia articles.
The vectors of two different texts can then be compared to assess the semantic similarity of those texts.

This implementation was written by Philip van Oosten.
It takes advantage of the mature Lucene project.

## License

This software is provided under the terms of the AGPLv3 license.
If this software seems helpful to you, but you dislike the licensing, don't let it get in your way and contact the author.
We can work something out.

## Usage

Each of the following steps takes several hours.

 * Download a Wikipedia dump
 * Indexing

After indexing, you are ready to transform text to vectors.

## Theory

Wikipedia-based Explicit Semantic Analysis, as described by Gabrilovich and Markovitch.

ESA is well described in a scientific paper.

http://en.wikipedia.org/wiki/Explicit_semantic_analysis

http://www.cs.technion.ac.il/~gabr/resources/code/esa/esa.html

http://www.cs.technion.ac.il/~gabr/papers/ijcai-2007-sim.pdf

