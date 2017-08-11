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
    <xsl:apply-templates  />
</xsl:template>



<xsl:template match="group">
      <h2 style="{$h-style}"><xsl:value-of select="@id" /> (group)</h2>
      <hr />

      created from specification
      <xsl:call-template name="GrSpecRef">
            <xsl:with-param name="id"><xsl:value-of select="@specification"/></xsl:with-param>
      </xsl:call-template>
      <xsl:if test="@parent-group = 'root'">
         (<i>root</i> group)
      </xsl:if>
      <xsl:if test="@parent-group != 'root'">
         (<i>subgroup</i> of
         <xsl:call-template name="GroupRef">
            <xsl:with-param name="id"><xsl:value-of select="@parent-group"/>)</xsl:with-param>
         </xsl:call-template>
      </xsl:if>

      <xsl:if test="@owner">
         - owner is
         <xsl:call-template name="AgentRef">
            <xsl:with-param name="id"><xsl:value-of select="@owner"/></xsl:with-param>
         </xsl:call-template>
      </xsl:if>

      <br/>

      <xsl:apply-templates select="well-formed" />

      <xsl:if test="$show-oe-img='true'">
          <img alt="" style="max-width:100%;width: expression(this.width > 100% ? 100%: true);" >
                <xsl:attribute name="src">
                    <xsl:value-of select="@id"/><xsl:text>.svg</xsl:text>
                </xsl:attribute>
          </img>
      </xsl:if>

      <xsl:apply-templates select="players" />

    <xsl:if test="count(responsible-for) > 0">

        <p><b style="{$txt-style}">Responsible for the following schemes:</b>
        <ul>
        <xsl:for-each select="responsible-for/scheme">
            <li> <span style="{$txt-style}">
                 <xsl:call-template name="SchemeRef">
                    <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
                </xsl:call-template>
                </span>
            </li>
        </xsl:for-each>
        </ul>
        </p>
    </xsl:if>

    <xsl:if test="count(subgroups) > 0">
        <p><b style="{$txt-style}">Subgroups</b>:
        <xsl:apply-templates select="subgroups" />
        </p>
    </xsl:if>
</xsl:template>

<xsl:template match="subgroups">
        <ul>
        <xsl:for-each select="group">
            <li>
            <span style="{$txt-style}">
                <xsl:call-template name="GroupRef">
                    <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
                </xsl:call-template>

                (formation <xsl:value-of select="well-formed"/>)
                <xsl:if test="count(players/role-player) > 0">
                    :
                    <ul>
                    <xsl:for-each select="players/role-player">
                          <li>
                             <xsl:call-template name="AgentRef">
                                <xsl:with-param name="id"><xsl:value-of select="@agent"/></xsl:with-param>
                             </xsl:call-template>

                          plays
                             <xsl:call-template name="RoleRef">
                                <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
                             </xsl:call-template>
                          </li>
                    </xsl:for-each>
                    </ul>
                </xsl:if>
                <xsl:if test="count(subgroups) > 0">
                    <xsl:apply-templates select="subgroups" />
                </xsl:if>
            </span>
            </li>
        </xsl:for-each>
        </ul>
</xsl:template>

</xsl:stylesheet>

