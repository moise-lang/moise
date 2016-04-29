TARGET  = ora4mas-basic

html: $(TARGET).tex Makefile
	latex $(TARGET).tex
	bibtex $(TARGET)
	latex $(TARGET).tex
	latex2html  -split 0 -style style.css \
              -white -image_type=png  \
              -dir html -mkdir -toc_stars \
              -show_section_numbers -noinfo -address "" -html_version 3.2,math \
              -no_navigation $(TARGET).tex
	cp html/ora4mas-basic.html .
	cp $(TARGET).html index.html
	make clean

clean:
	rm -f $(TARGET).ps *.log $(TARGET).aux $(TARGET).out $(TARGET).dvi $(TARGET).blg || true
	rm -rf auto || true
	rm -rf *pdf || true
	find . -name *bak -exec rm {} \; || true
	find . -name *~ -exec rm {} \; || true
	rm -rf html

publish:
	scp index.html jomifred,moise@web.sf.net:/home/project-web/moise/htdocs/doc/ora4mas
	scp *.css jomifred,moise@web.sf.net:/home/project-web/moise/htdocs/doc/ora4mas
	scp *.png jomifred,moise@web.sf.net:/home/project-web/moise/htdocs/doc/ora4mas
