<?xml version="1.0" ?>

<xsl:stylesheet  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   version="1.0">

<xsl:param name="h-style" select="'color: red; font-family: arial;'" />
<xsl:param name="h3-style" select="'color: green; font-family: arial;'" />
<xsl:param name="txt-style" select="'font-family: arial;'" />

<xsl:param name="th-style" select="'text-align: left; vertical-align: top;  color: #330099;'" />
<xsl:param name="th-style2" select="'text-align: left; color: blue;'" />
<xsl:param name="td-style"  select="'text-align: left; vertical-align: top;'" />
<xsl:param name="td-style2" select="'text-align: center; vertical-align: top;'" />
<xsl:param name="trh-style" select="'background-color: #ece7e6; font-family: arial; vertical-align: top;'" />
<xsl:param name="tr-style"  select="'background-color: #ece7e6; font-family: arial;'" />

<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="functions.xsl" />

<xsl:template match="organisational-entity">
   <html>
      <body>
         <h2 style="{$h-style}"><xsl:value-of select="@os" /> (Organisational Entity)</h2>
         <xsl:apply-templates select="agents" />
         <xsl:apply-templates select="groups" />
         <xsl:apply-templates select="schemes" />
      </body>
   </html>
</xsl:template>


<xsl:template match="agents">
  <hr/><h3 style="{$h3-style}">Agents</h3>
     <ul>
     <xsl:apply-templates select="agent" >
         <xsl:sort select="@id" />
     </xsl:apply-templates>
     </ul>
</xsl:template>


<xsl:template match="agent">
    <li>
      <span style="{$txt-style}">
      <xsl:call-template name="AgentRef">
            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
      </xsl:call-template>
      </span>
      <!-- <xsl:apply-templates select="Roles" /> -->
    </li>
</xsl:template>

<xsl:template match="roles">
     <ul>
     <xsl:apply-templates select="role-player" />
     </ul>
</xsl:template>


<xsl:template match="role-player">
    <li>
      <span style="{$txt-style}">
      <xsl:call-template name="AgentRef">
            <xsl:with-param name="id"><xsl:value-of select="@agent"/></xsl:with-param>
      </xsl:call-template>
      plays 
      <xsl:call-template name="RoleRef">
            <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
      </xsl:call-template>
      </span>
    </li>
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
      created from specification 
      <xsl:call-template name="GrSpecRef">
            <xsl:with-param name="id"><xsl:value-of select="@specification"/></xsl:with-param>
      </xsl:call-template>
      <br/>
      </span>
      
      <xsl:apply-templates select="well-formed" />
      <xsl:apply-templates select="players" />
      <xsl:apply-templates select="subgroups" />
    </li>
</xsl:template>


<xsl:template match="well-formed">
   
    <br/><b style="{$txt-style}">Formation:</b> 
    <blockquote>
        <span style="{$txt-style}">
        <xsl:apply-templates />
        </span>
    </blockquote>
</xsl:template>

<xsl:template match="players">
    <br/><b style="{$txt-style}">Players</b>
    <ul>
    <xsl:apply-templates select="role-player" >
         <xsl:sort select="@agent" />
    </xsl:apply-templates>
    <xsl:apply-templates select="mission-player" >
         <xsl:sort select="@agent" />
    </xsl:apply-templates>
    </ul>
</xsl:template>

<xsl:template match="subgroups">
    <br/><b style="{$txt-style}">Subgroups</b>
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
      <br/>
      <xsl:apply-templates select="well-formed" />
      <xsl:apply-templates select="responsible-groups" />
      <br/>
      
      <xsl:apply-templates select="players" />
      <br/>
      <xsl:apply-templates select="goals" />
      <br/>
      <br/>
      </span>
    </li>
</xsl:template>

<xsl:template match="responsible-groups">
    <xsl:if test="count(group)=0">
        <b style="{$txt-style}">no</b> group is responsible for this scheme!
    </xsl:if>
    <xsl:if test="count(group)>0">
    
        <b style="{$txt-style}">Responsible groups</b>:
            <span style="{$txt-style}">
            <xsl:for-each select="group">
                <xsl:call-template name="GroupRef">
                    <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
                </xsl:call-template>
                <xsl:if test="not(position()=last())"><xsl:text>, </xsl:text></xsl:if>
            </xsl:for-each>
            </span>
    </xsl:if>
    <br/>
</xsl:template>


<xsl:template match="mission-player">
    <li>
      <span style="{$txt-style}">
      <xsl:call-template name="AgentRef">
            <xsl:with-param name="id"><xsl:value-of select="@agent"/></xsl:with-param>
      </xsl:call-template>
      committed to 
      <xsl:call-template name="MissionRef">
            <xsl:with-param name="id"><xsl:value-of select="@mission"/></xsl:with-param>
      </xsl:call-template>
      </span>
    </li>
</xsl:template>

<xsl:template match="goals">
         <table border="0" cellspacing="3" cellpadding="6">
         <tr style="{$trh-style}"> 
         <th valign="top" style="{$th-style}">goal</th>
         <th valign="top" style="{$th-style}">state</th> 
         <th valign="top" style="{$th-style}">committed / achieved by</th>
         <th valign="top" style="{$th-style}">arguments</th>
         <th valign="top" style="{$th-style}">plan : dependencies </th> 
         </tr>
         <xsl:apply-templates select="goal[@root='true']" />
         </table>
</xsl:template>

<xsl:template match="goal">
      <tr style="{$trh-style}">
      <td style="{$td-style}">
      <pre><xsl:value-of select="@depth"/><xsl:value-of select="@specification"/></pre></td>
      
      <td style="{$td-style}">
         <xsl:value-of select="@state"/>
         <xsl:if test="@satisfied-ags != '[]'">
            : <xsl:value-of select="@satisfied-ags"/>
         </xsl:if> 
      </td>
      <td style="{$td-style}"><xsl:value-of select="@committed-ags"/>/<xsl:value-of select="@achieved-by"/></td>
      <td style="{$td-style}">
      <xsl:if test="count(argument)>0">
        <xsl:for-each select="argument">
           <xsl:value-of select="@id"/> = <xsl:value-of select="@value"/>
           <xsl:if test="not(position()=last())">
              <br/>
           </xsl:if>
        </xsl:for-each>
      </xsl:if>
      </td>

      <td style="{$td-style}">
      <xsl:if test="count(plan)>0">
          <xsl:apply-templates select="plan" />
      </xsl:if>
      <xsl:if test="count(depends-on)>0">
          <font size="-1">
          <xsl:if test="count(plan)>0">
              <xsl:text> : </xsl:text>
          </xsl:if>
          <xsl:text> { </xsl:text>
          <i>           
          <xsl:for-each select="depends-on" >
                <xsl:if test="@explicit = 'true'">
                   <b><xsl:value-of select="@goal"/></b>
                </xsl:if>
                <xsl:if test="count(@explicit) = 0">
                   <xsl:value-of select="@goal"/>
                </xsl:if>
                <xsl:if test="not(position()=last())"><xsl:text>, </xsl:text></xsl:if>
          </xsl:for-each>
          </i><xsl:text> }</xsl:text>
          </font>
      </xsl:if>
      </td>
      </tr>
      
      <xsl:if test="count(plan)>0">
          <xsl:for-each select="plan/goal" >
              <xsl:variable name="subGoal" select="@id" />
              <xsl:apply-templates select="../../../goal[@specification=$subGoal]"/>
          </xsl:for-each>
      </xsl:if>
      
</xsl:template>

<xsl:template match="plan">
                    <xsl:variable name="pOp" select='@operator'/> 
                    <xsl:if test="string-length(@successRate)>0">
                        <sub>(<xsl:value-of select="@successRate"/>)&#160;</sub>
                    </xsl:if>
                    <xsl:for-each select="goal">
                        <xsl:value-of select="@id"/>
                        <xsl:if test="not(position()=last())">
                            <xsl:if test="$pOp='sequence'">, </xsl:if>
                            <xsl:if test="$pOp='choice'"> | </xsl:if>
                            <xsl:if test="$pOp='parallel'"> || </xsl:if>
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:apply-templates select="property" />
</xsl:template>


</xsl:stylesheet>
