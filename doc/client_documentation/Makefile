
all: client_documentation.pdf
	@okular client_documentation.pdf &

client_documentation.pdf: *.tex Makefile
	pdflatex -halt-on-error -interaction=errorstopmode client_documentation.tex

clean:
	@rm -f *.out *.toc *.aux *.dvi *.log *.pdf
