<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="oe.xsl" />

<xsl:param name="show-oe-img"    select="'false'" />

<xsl:template match="/">
    <xsl:apply-templates />
</xsl:template>


<xsl:template match="scheme">
      <h2 style="{$h-style}"><xsl:value-of select="@id" /> (scheme instance)</h2>
      <hr />

      created from specification
      <xsl:call-template name="SchemeSpecRef">
            <xsl:with-param name="id"><xsl:value-of select="@specification"/></xsl:with-param>
      </xsl:call-template>

      <xsl:if test="@owner">
         , owner is
         <xsl:call-template name="AgentRef">
            <xsl:with-param name="id"><xsl:value-of select="@owner"/></xsl:with-param>
         </xsl:call-template>
      </xsl:if>
      <br/>

      <xsl:apply-templates select="well-formed" />
      <xsl:apply-templates select="responsible-groups" />
      <br/>

      <xsl:if test="$show-oe-img='true'">
          <img alt="" style="max-width:100%;width: expression(this.width > 100% ? 100%: true);" >
                <xsl:attribute name="src">
                    <xsl:value-of select="@id"/><xsl:text>.svg</xsl:text>
                </xsl:attribute>
          </img>
      </xsl:if>

      <xsl:apply-templates select="players" />
      <xsl:apply-templates select="goals" />

</xsl:template>

</xsl:stylesheet>

