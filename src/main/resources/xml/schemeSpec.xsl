<?xml version="1.0" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
     xmlns:os='http://moise.sourceforge.net/os'
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="os.xsl" />

<xsl:param name="schemeSpecId"/>

<xsl:template match="/">
    <h2 style="{$h1-style}">Scheme: <xsl:value-of select="$schemeSpecId" /></h2>
    <hr />
    <xsl:apply-templates select="//os:scheme[@id = $schemeSpecId]" />
</xsl:template>


</xsl:stylesheet>
