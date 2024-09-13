<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
     xmlns:os='http://moise.sourceforge.net/os'
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="os.xsl" />

<xsl:param name="missionId"/>

<xsl:template match="/">
    <xsl:apply-templates select="os:organisational-specification/os:functional-specification/os:scheme/os:mission[@id=$missionId]" />
</xsl:template>


</xsl:stylesheet>

