<?xml version="1.0" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
     xmlns:os='https://moise-lang.github.io/os'
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="os.xsl" />

<xsl:param name="grSpecId"/>

<xsl:template match="/">
    <xsl:apply-templates select="//os:group-specification[@id = $grSpecId]" />
</xsl:template>


</xsl:stylesheet>
