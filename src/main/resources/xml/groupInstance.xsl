<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="oe.xsl" />

<xsl:param name="groupId"/>

<xsl:template match="/">
    <xsl:apply-templates select="organisational-entity/groups/group" />
</xsl:template>



<xsl:template match="group">
    <xsl:if test="@id=$groupId">
      <h2 style="{$h-style}"><xsl:value-of select="@id" /> (group)</h2>
      <hr />

      created from specification
      <xsl:call-template name="GrSpecRef">
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
      <xsl:apply-templates select="players" />

    </xsl:if>
    <xsl:apply-templates select="subgroups" />
</xsl:template>

<xsl:template match="subgroups">
    <xsl:apply-templates select="group" />
</xsl:template>

</xsl:stylesheet>

