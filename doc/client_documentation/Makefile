
DOT_SOURCES=$(wildcard *.dot)
DOT_OUTPUTS=$(DOT_SOURCES:.dot=.svg)
DOT_PDFS=$(DOT_SOURCES:.dot=.pdf)

all: client_documentation.pdf
	@okular client_documentation.pdf &

client_documentation.pdf: *.tex $(DOT_PDFS) Makefile
	pdflatex -halt-on-error -interaction=errorstopmode client_documentation.tex
	pdflatex -halt-on-error -interaction=errorstopmode client_documentation.tex

$(DOT_OUTPUTS): %.svg : %.dot Makefile
	@dot $*.dot -Tsvg -o$*.svg

$(DOT_PDFS): %.pdf : %.svg Makefile
	@inkscape $*.svg --export-pdf=$*.pdf

clean:
	@rm -f *.out *.toc *.aux *.dvi *.log *.pdf $(DOT_OUTPUTS) $(DOT_PDFS)
