<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="oe.xsl" />

<xsl:param name="agentId"/>

<xsl:template match="/">
    <xsl:apply-templates select="organisational-entity/agents/agent" />
</xsl:template>


<xsl:template match="agent">
    <xsl:if test="@id=$agentId">
      <h2 style="{$h-style}">
          <xsl:value-of select="@id" /> (agent)
      </h2>

      <table border="0" cellspacing="3" cellpadding="6">
      <xsl:if test="count(obligation)>0">
          <tr style="{$trh-style}">
          <th valign="top" style="{$th-style}">Obligations</th>
          <td style="{$td-style}">
          <xsl:value-of select="text()" />
          <blockquote>
          <table cellspacing="1" cellpadding="4">
          <tr style="{$trh-style}">
          <th valign="top" style="{$th-style2}">role</th>
          <th valign="top" style="{$th-style2}">group</th>
          <th valign="top" style="{$th-style2}">mission</th>
          <th valign="top" style="{$th-style2}">scheme</th>
          </tr>
          <xsl:for-each select="obligation">
            <tr style="{$trh-style}">
            <td style="{$td-style}">
               <xsl:call-template name="RoleRef">
                    <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
               </xsl:call-template>
            </td>
            <td style="{$td-style}">
               <xsl:call-template name="GroupRef">
                    <xsl:with-param name="id"><xsl:value-of select="@group"/></xsl:with-param>
               </xsl:call-template>
            </td>
            <td style="{$td-style}">
               <xsl:call-template name="MissionRef">
                    <xsl:with-param name="id"><xsl:value-of select="@mission"/></xsl:with-param>
               </xsl:call-template>
            </td>
            <td style="{$td-style}">
               <xsl:call-template name="SchemeRef">
                    <xsl:with-param name="id"><xsl:value-of select="@scheme"/></xsl:with-param>
               </xsl:call-template>
            </td>
            </tr>
          </xsl:for-each>
          </table>
          </blockquote>
          </td>
          </tr>
      </xsl:if>
      
      <tr style="{$trh-style}">
          <th valign="top" style="{$th-style}">Roles</th>
          <td style="{$td-style}"><xsl:apply-templates select="../../groups/group" /></td>
      </tr>
      
      <tr style="{$trh-style}">
          <th valign="top" style="{$th-style}">Missions</th>
          <td style="{$td-style}"><xsl:apply-templates select="../../schemes/scheme" /></td>
      </tr>
      
      <xsl:if test="count(permission)>0">
          <tr style="{$trh-style}">
          <th valign="top" style="{$th-style}">Permitted missions</th>
          <td style="{$td-style}">
          <table border="0" cellspacing="1" cellpadding="4">
          <tr style="{$trh-style}">
          <th valign="top" style="{$th-style2}">role</th>
          <th valign="top" style="{$th-style2}">group</th>
          <th valign="top" style="{$th-style2}">mission</th>
          <th valign="top" style="{$th-style2}">scheme</th>
          </tr>
          <xsl:for-each select="permission">
            <tr style="{$trh-style}">
            <td style="{$td-style}">
               <xsl:call-template name="RoleRef">
                    <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
               </xsl:call-template>
            </td>
            <td style="{$td-style}">
               <xsl:call-template name="GroupRef">
                    <xsl:with-param name="id"><xsl:value-of select="@group"/></xsl:with-param>
               </xsl:call-template>
            </td>
            <td style="{$td-style}">
               <xsl:call-template name="MissionRef">
                    <xsl:with-param name="id"><xsl:value-of select="@mission"/></xsl:with-param>
               </xsl:call-template>
            </td>
            <td style="{$td-style}">
               <xsl:call-template name="SchemeRef">
                    <xsl:with-param name="id"><xsl:value-of select="@scheme"/></xsl:with-param>
               </xsl:call-template>
            </td>
            </tr>
          </xsl:for-each>
          </table>
          </td>
          </tr>
      </xsl:if>

      <xsl:if test="count(possibleGoal)>0">
          <tr style="{$trh-style}">
          <th valign="top" style="{$th-style}">Permitted goals</th>
          <td style="{$td-style}">
          <table border="0" cellspacing="1" cellpadding="4">
          <tr style="{$trh-style}">
          <th valign="top" style="{$th-style2}">goal</th>
          <th valign="top" style="{$th-style2}">scheme</th>
          </tr>
          <xsl:for-each select="possibleGoal">
            <tr style="{$trh-style}">
            <td style="{$td-style}">
               <xsl:call-template name="GoalRef">
                    <xsl:with-param name="id"><xsl:value-of select="@goal"/></xsl:with-param>
               </xsl:call-template>
            </td>
            <td style="{$td-style}">
               <xsl:call-template name="SchemeRef">
                    <xsl:with-param name="id"><xsl:value-of select="@scheme"/></xsl:with-param>
               </xsl:call-template>
            </td>
            </tr>
          </xsl:for-each>
          </table>
          </td>
          </tr>
      </xsl:if>
      </table>
    </xsl:if>
</xsl:template>


<xsl:template match="group">
      <xsl:apply-templates select="players" />
      <xsl:apply-templates select="subgroups" />
</xsl:template>

<xsl:template match="subgroups">
     <xsl:apply-templates select="group" />
</xsl:template>

<xsl:template match="scheme">
      <xsl:apply-templates select="players" />
</xsl:template>

<xsl:template match="players">
     <xsl:apply-templates select="role-player" />
     <xsl:apply-templates select="mission-player" />
</xsl:template>



<xsl:template match="role-player">
    <xsl:if test="@agent=$agentId">
      plays 
      <xsl:call-template name="RoleRef">
            <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
      </xsl:call-template>
      in 
      <xsl:call-template name="GroupRef">
            <xsl:with-param name="id"><xsl:value-of select="../../@id"/></xsl:with-param>
      </xsl:call-template>
      <br/>
    </xsl:if>
</xsl:template>

<xsl:template match="mission-player">
    <xsl:if test="@agent=$agentId">
      committed to 
      <xsl:call-template name="MissionRef">
            <xsl:with-param name="id"><xsl:value-of select="@mission"/></xsl:with-param>
      </xsl:call-template>
      in 
      <xsl:call-template name="SchemeRef">
            <xsl:with-param name="id"><xsl:value-of select="../../@id"/></xsl:with-param>
      </xsl:call-template>
      <br/>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>

