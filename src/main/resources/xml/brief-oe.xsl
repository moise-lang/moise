<?xml version="1.0" ?>

<xsl:stylesheet  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   version="1.0">

<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="oe.xsl" />

<xsl:template match="agents">
  <hr/><h3 style="{$h3-style}">Agents</h3>
  <xsl:for-each select="agent">
      <xsl:value-of select="@id" />
      <xsl:text>; </xsl:text>
  </xsl:for-each>
</xsl:template>


<xsl:template match="role-player">
      <span style="{$txt-style}">
      <xsl:call-template name="AgentRef">
            <xsl:with-param name="id"><xsl:value-of select="@agent"/></xsl:with-param>
      </xsl:call-template>

      <xsl:text> (</xsl:text>
      <xsl:call-template name="RoleRef">
            <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
      </xsl:call-template>
      <xsl:text>)</xsl:text>
      </span>
      <xsl:text>; </xsl:text>
</xsl:template>


<xsl:template match="groups">
  <hr/><h3 style="{$h3-style}">Groups</h3>
     <ul>
     <xsl:apply-templates select="group" >
         <xsl:sort select="@id" />
     </xsl:apply-templates>
     </ul>
</xsl:template>

<xsl:template match="group">
    <li>
      <span style="{$txt-style}">
      <xsl:call-template name="GroupRef">
            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
      </xsl:call-template>
      <xsl:text> (</xsl:text>
      <xsl:call-template name="GrSpecRef">
            <xsl:with-param name="id"><xsl:value-of select="@specification"/></xsl:with-param>
      </xsl:call-template>
      <xsl:text>)</xsl:text>
      </span>

      <xsl:apply-templates select="players" />
      <xsl:apply-templates select="subgroups" />
    </li>
</xsl:template>

<xsl:template match="players">
    <b style="{$txt-style}"> players (<xsl:value-of select="count(role-player | mission-player)" />): </b>
    <xsl:apply-templates select="role-player" >
         <xsl:sort select="@agent" />
    </xsl:apply-templates>
    <xsl:apply-templates select="mission-player" >
         <xsl:sort select="@agent" />
    </xsl:apply-templates>
</xsl:template>

<xsl:template match="subgroups">
     <ul>
     <xsl:apply-templates select="group" >
         <xsl:sort select="@id" />
     </xsl:apply-templates>
     </ul>
</xsl:template>


<xsl:template match="schemes">
  <hr/><h3 style="{$h3-style}">Schemes</h3>
     <ul>
     <xsl:apply-templates select="scheme" />
     </ul>
</xsl:template>

<xsl:template match="scheme">
    <li>
      <span style="{$txt-style}">
      <xsl:call-template name="SchemeRef">
            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
      </xsl:call-template>
      <xsl:text> (</xsl:text>
      <xsl:call-template name="SchemeSpecRef">
            <xsl:with-param name="id"><xsl:value-of select="@specification"/></xsl:with-param>
      </xsl:call-template>
      <xsl:text>)</xsl:text>
      <xsl:apply-templates select="responsible-groups" />
      <xsl:apply-templates select="players" />
      <br/>
      <!--xsl:apply-templates select="goals" /-->
      <br/>
      </span>
    </li>
</xsl:template>

<xsl:template match="responsible-groups">
    <b style="{$txt-style}"> responsible groups</b>:
            <span style="{$txt-style}">
            <xsl:for-each select="group">
                <xsl:call-template name="GroupRef">
                    <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
                </xsl:call-template>,
            </xsl:for-each>
            </span>
</xsl:template>


<xsl:template match="mission-player">
      <span style="{$txt-style}">
      <xsl:call-template name="AgentRef">
            <xsl:with-param name="id"><xsl:value-of select="@agent"/></xsl:with-param>
      </xsl:call-template>
      <xsl:text> (</xsl:text>
      <xsl:call-template name="MissionRef">
            <xsl:with-param name="id"><xsl:value-of select="@mission"/></xsl:with-param>
      </xsl:call-template>
      </span>
      <xsl:text>)</xsl:text>
      <xsl:text>; </xsl:text>
</xsl:template>

</xsl:stylesheet>
