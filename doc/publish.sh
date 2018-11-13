#
# by Jomi
#

cd ..
gradle renderAsciidoc
gradle javadoc
cd doc
cp readme.html index.html
scp -r *  jomifred,moise@web.sf.net:/home/project-web/moise/htdocs/doc
