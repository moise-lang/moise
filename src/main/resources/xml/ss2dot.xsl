<?xml version="1.0" ?>

<!-- translate moise OS file to a DOT graph -->

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"

     xmlns:os='http://moise.sourceforge.net/os'
>

<xsl:output encoding="ISO-8859-1" method="text" />
<xsl:strip-space elements="*"/>


<xsl:template match="organisational-specification">
    <xsl:text>digraph </xsl:text>
    <xsl:value-of select="@id" />
    <xsl:text> {
    </xsl:text>
    <xsl:text>rankdir=BT;

    </xsl:text>
    <xsl:apply-templates select="structural-specification" />
    <xsl:text>
}</xsl:text>
</xsl:template>

<xsl:template match="structural-specification">
    <xsl:text >/* role hierarchy */
    </xsl:text>
    <xsl:text>soc [fontname="Italic"];
    </xsl:text>
    <xsl:apply-templates select="role-definitions/os:role" />
    <xsl:apply-templates select="group-specification" />
</xsl:template>

<xsl:template match="role">
    <xsl:if test="count(extends)>0">
        <xsl:for-each select="extends">
            <xsl:value-of select="@id"/>
            <xsl:text> -> <xsl:value-of select="@role"/> [arrowhead=onormal,arrowsize=1.5];
    </xsl:text>
        </xsl:for-each>
    </xsl:if>
    <xsl:if test="count(extends)=0">
        <xsl:value-of select="@id"/>
        <xsl:text> -> soc;
    </xsl:text>
    </xsl:if>
</xsl:template>


<xsl:template match="os:group-specification">
</xsl:template>

</xsl:stylesheet>
