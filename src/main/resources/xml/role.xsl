<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
     xmlns:os='http://moise.sourceforge.net/os'
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="functions.xsl" />

<xsl:param name="roleId"/>

<xsl:template match="/">
    <xsl:apply-templates select="role" />
</xsl:template>

<xsl:template match="role">
    <xsl:if test="@id=$roleId">
      <h2><xsl:value-of select="@id" /> (role)</h2>
      <hr />

      <xsl:if test="count(extends)>0">
        <b>Extends</b>
        <ul><xsl:apply-templates select="extends" /></ul>
      </xsl:if>

      <xsl:apply-templates select="properties" />

      <xsl:if test="count(specialization)>0">
        <b>Specializations</b>
        <ul><xsl:apply-templates select="specialization" /></ul>
      </xsl:if>

      <xsl:if test="count(group)=0">
        it is an <b>abstract</b> role (it can not be played).
      </xsl:if>

      <xsl:if test="count(group)>0">
        Can be played in the following <b>groups</b>
        <ul><xsl:apply-templates select="group" /></ul>
      </xsl:if>

      <xsl:if test="count(deontic-relation)>0">
        This role has the following <b>deontic relations</b> to missions
        <ul><xsl:apply-templates select="deontic-relation" /></ul>
      </xsl:if>

    </xsl:if>
</xsl:template>


<xsl:template match="extends">
            <li>
            <xsl:call-template name="RoleRef">
                <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
            </xsl:call-template>
            <xsl:if test="count(extends)>0">
            <ul><xsl:apply-templates select="os:extends" /></ul>
            </xsl:if>
            </li>
</xsl:template>



<xsl:template match="specialization">
    <li>
            <xsl:call-template name="RoleRef">
                <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
            </xsl:call-template>
    </li>
</xsl:template>


<xsl:template match="group">
    <li>
        <xsl:call-template name="GrSpecRef">
            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
        </xsl:call-template>

        <xsl:if test="count(link)>0">
            using the following links
            <ul><xsl:apply-templates select="link" /></ul>
        </xsl:if>

        <xsl:if test="count(compatibility)>0">
            and the following  compatibilities
            <ul><xsl:apply-templates select="compatibility" /></ul>
        </xsl:if>
    </li>
</xsl:template>



<xsl:template match="link">
            <li>
            <xsl:call-template name="RoleRef">
               <xsl:with-param name="id"><xsl:value-of select="@from" /></xsl:with-param>
            </xsl:call-template>
            <xsl:text> </xsl:text>
            <i><xsl:value-of select="@type" /></i> link to
            <xsl:call-template name="RoleRef">
               <xsl:with-param name="id"><xsl:value-of select="@to" /></xsl:with-param>
            </xsl:call-template>
            (<xsl:value-of select="@scope" />)
            , defined in the group
                <xsl:call-template name="GrSpecRef">
                    <xsl:with-param name="id"><xsl:value-of select="@gr-id"/></xsl:with-param>
                </xsl:call-template>
            </li>
</xsl:template>

<xsl:template match="compatibility">
            <li>
            <xsl:call-template name="RoleRef">
               <xsl:with-param name="id"><xsl:value-of select="@from" /></xsl:with-param>
            </xsl:call-template>
            compatible with
            <xsl:call-template name="RoleRef">
               <xsl:with-param name="id"><xsl:value-of select="@to" /></xsl:with-param>
            </xsl:call-template>
            (<xsl:value-of select="@scope" />)
            , defined in the group
                <xsl:call-template name="GrSpecRef">
                    <xsl:with-param name="id"><xsl:value-of select="@gr-id"/></xsl:with-param>
                </xsl:call-template>
            </li>
</xsl:template>


<xsl:template match="deontic-relation">
     <li>
         has <i><xsl:value-of select="@type"/> </i>
         for the mission
         <xsl:call-template name="MissionRef">
            <xsl:with-param name="id"><xsl:value-of select="@mission"/></xsl:with-param>
         </xsl:call-template>
         <xsl:if test="@time-constraint != ''">
             with the "<xsl:value-of select="@time-constraint"/>"  time constraint
         </xsl:if>
     </li>
</xsl:template>



</xsl:stylesheet>

