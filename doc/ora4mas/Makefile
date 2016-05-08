clean:
	find . -name *bak -exec rm {} \; || true
	find . -name *~ -exec rm {} \; || true
	rm ../../src/examples/writePaper/agents/*ref.asl
	rm ../../src/examples/writePaper/agents/*ref.mas2j
	rm -rf html
	rm *-ref.adoc

adoc:
	asciidoc-bib -b ~/pubs/bib/jomi.bib ora4mas.adoc
	asciidoctor ora4mas-ref.adoc
	mv ora4mas-ref.html index.html
	rm ora4mas.html

publish:
	scp index.html jomifred,moise@web.sf.net:/home/project-web/moise/htdocs/doc/ora4mas
	#scp *.css jomifred,moise@web.sf.net:/home/project-web/moise/htdocs/doc/ora4mas
	scp *.png jomifred,moise@web.sf.net:/home/project-web/moise/htdocs/doc/ora4mas
