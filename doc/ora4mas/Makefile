clean:
	find . -name *bak -exec rm {} \; || true
	find . -name *~ -exec rm {} \; || true
	rm ../../src/examples/writePaper/agents/*ref.asl
	rm ../../src/examples/writePaper/agents/*ref.mas2j
	rm -rf html
	rm *-ref.adoc

adoc:
	asciidoc-bib -b ~/pubs/bib/jomi.bib readme.adoc
	asciidoctor readme-ref.adoc
	mv readme-ref.html index.html
	rm readme.html

publish:
	scp index.html jomifred,moise@web.sf.net:/home/project-web/moise/htdocs/doc/ora4mas
	#scp *.css jomifred,moise@web.sf.net:/home/project-web/moise/htdocs/doc/ora4mas
	scp *.png jomifred,moise@web.sf.net:/home/project-web/moise/htdocs/doc/ora4mas
