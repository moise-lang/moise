#!/bin/sh

export BIBINPUTS=$HOME/pubs/bib:

rm *aux *dvi *log *toc *bbl *blg *~ *.backup

for f in `ls y*tex`; do
  n=`echo $f | cut -d . -f 1`
  echo generating $n
  latex --interaction nonstopmode $n > /dev/null
  bibtex $n > /dev/null
  #latex --interaction nonstopmode $n > /dev/null
done


latex --interaction nonstopmode related-papers.tex > /dev/null
latex --interaction nonstopmode related-papers.tex > out

echo "generating page (see out file for the result)"

#latex2html -split 0 -white -image_type=gif -transparent -math -no_navigation publicationsWebPage.tex >> out

latex2html -split 0 -white -transparent -math -no_navigation related-papers.tex >> out

cp related-papers/related-papers.html .

#dvipdfm publicationsWebPage

rm *aux *dvi *log *toc *bbl *blg *.out

