<?xml version="1.0" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
     
     xmlns:os='http://moise.sourceforge.net/os'
>
    
    
<xsl:param name="h1-style" select="'color: red; font-family: arial;'" />
<xsl:param name="h2-style" select="'color: red; font-family: arial;'" />
<xsl:param name="h3-style" select="'color: green; font-family: arial;'" />
<xsl:param name="txt-style" select="'font-family: arial;'" />

<xsl:param name="th-style"  select="'text-align: left; vertical-align: top;  color: #330099;'" />
<xsl:param name="td-style"  select="'text-align: left; vertical-align: top; font-family: arial;'" />
<xsl:param name="trh-style" select="'background-color: #ece7e6; font-family: arial; vertical-align: top;'" />
<xsl:param name="tr-style"  select="'background-color: #ece7e6; font-family: arial;'" />
    
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="functions.xsl" />


<xsl:template match="os:organisational-specification">
   <html>
      <head>
        <title><xsl:value-of select="@id" /></title>
      </head>
   
      <body>
         <h1 style="{$h1-style}">Organisational Specification: <i><xsl:value-of select="@id" /></i></h1>
         <xsl:apply-templates select="os:properties" />
         
         <xsl:apply-templates select="os:structural-specification" />
         <xsl:apply-templates select="os:functional-specification" />
         <xsl:apply-templates select="os:normative-specification" />
      </body>
   </html>
</xsl:template>


<xsl:template match="os:structural-specification">
    <hr/><h2 style="{$h2-style}">Structural Specification</h2>
  
    <xsl:apply-templates select="os:properties" />

    <h3 style="{$h3-style}">Roles</h3>
    <ul>
    <xsl:apply-templates select="os:role-definitions/os:role" />
    </ul>
    <xsl:apply-templates select="os:group-specification" />
</xsl:template>


<xsl:template match="os:role">
    <li style="{$txt-style}">
        <xsl:call-template name="RoleRef">
            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
        </xsl:call-template>
        extends
        <xsl:if test="count(os:extends)>0">
            <xsl:for-each select="os:extends">
                <xsl:call-template name="RoleRef">
                    <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
                </xsl:call-template>
                <xsl:if test="not(position()=last())">,</xsl:if>
            </xsl:for-each>.
        </xsl:if>
        <xsl:if test="count(os:extends)=0">
            soc.
        </xsl:if>
  </li>
</xsl:template>


<xsl:template match="os:group-specification">
    <xsl:param name="gid" select="@id"/>
    <h3 style="{$h3-style}">Group <i><xsl:value-of select="@id" /></i></h3>
    <a id="{$gid}" />
    
    <blockquote>
    <!--
    Defined in
       <xsl:call-template name="GrSpecRef">
           <xsl:with-param name="id"><xsl:value-of select="../../@id" /></xsl:with-param>
       </xsl:call-template>
    -->
    <xsl:apply-templates select="os:properties" />

    <xsl:if test="count(os:roles/os:role)>0">
        <span style="{$txt-style}">
        <xsl:text>Possible roles: </xsl:text>
        <xsl:for-each select="os:roles/os:role">
            <xsl:call-template name="RoleRef">
               <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
            </xsl:call-template>            
            <xsl:if test="not(position()=last())"><xsl:text>, </xsl:text></xsl:if>
        </xsl:for-each>
        <xsl:text>.</xsl:text>
        </span>
    </xsl:if>


    <!-- xsl:if test="count(os:subgroups/os:group-specification)>0">
        <p/><xsl:text>Subgroups:</xsl:text>
        <xsl:for-each select="os:subgroups/os:group-specification">
            <xsl:call-template name="GrSpecRef">
               <xsl:with-param name="id"><xsl:value-of select="@id" /></xsl:with-param>
            </xsl:call-template>
            <xsl:if test="not(position()=last())"><xsl:text>, </xsl:text></xsl:if>
        </xsl:for-each>
        <xsl:text>.</xsl:text>
    </xsl:if -->

    <blockquote>
        <xsl:apply-templates select="os:subgroups/os:group-specification" />
    </blockquote>
    
    <xsl:apply-templates select="os:links">
        <xsl:with-param name="soExtends">false</xsl:with-param>
    </xsl:apply-templates>

    <xsl:apply-templates select="os:formation-constraints">
        <xsl:with-param name="soExtends">false</xsl:with-param>
    </xsl:apply-templates>
    </blockquote>

</xsl:template>



<!--
          * Links *
-->
<xsl:template match="os:links">
    <xsl:param name="soExtends" select="false" />
    <span style="{$txt-style}">
    <xsl:if test="$soExtends='false'">
        <xsl:text>Local links: </xsl:text>
    </xsl:if>
    <xsl:if test="$soExtends='true'">
        <xsl:text>Links from </xsl:text>
            <xsl:call-template name="GrSpecRef">
               <xsl:with-param name="id"><xsl:value-of select="../@id"/></xsl:with-param>
            </xsl:call-template>
            <xsl:text>: </xsl:text>
    </xsl:if>
    <ul>
    <xsl:for-each select="os:link">
        <xsl:if test="($soExtends='true' and @extends-subgroups='true') or $soExtends='false'">
            <li style="{$txt-style}">
            <xsl:call-template name="RoleRef">
               <xsl:with-param name="id"><xsl:value-of select="@from" /></xsl:with-param>
            </xsl:call-template> 
            <xsl:text> has a </xsl:text>
            <i><xsl:value-of select="@type" /></i>
            <xsl:text> link to </xsl:text>
            <xsl:call-template name="RoleRef">
               <xsl:with-param name="id"><xsl:value-of select="@to" /></xsl:with-param>
            </xsl:call-template>
            <xsl:if test="@bi-dir='true'">
                <xsl:text> and vice versa </xsl:text>
            </xsl:if>
            <xsl:text> (</xsl:text>
            <xsl:value-of select="@scope" />
            
            <xsl:if test="$soExtends='false'">
                <xsl:text>, </xsl:text>
                <xsl:if test="@extends-subgroups='true'">extends to subgroups</xsl:if>
                <xsl:if test="@extends-subgroups='false'">does not extend to subgroups</xsl:if>
            </xsl:if>
            <xsl:text>)</xsl:text>
            <xsl:apply-templates select="os:properties" />
            </li>
        </xsl:if>
    </xsl:for-each>
    </ul>
    
    <xsl:apply-templates select="../../../os:links">
         <xsl:with-param name="soExtends">true</xsl:with-param>
    </xsl:apply-templates>
    </span>
</xsl:template>




<!--
          * Constraint Formation *
-->
<xsl:template match="os:formation-constraints">
    <xsl:param name="soExtends" select="false" />

    <span style="{$txt-style}">
    <xsl:if test="$soExtends='false'">
        Constraint Formation
    </xsl:if>
        
    <ul>
    <xsl:if test="count(os:cardinality)>0 and $soExtends='false'" >
        <li style="{$txt-style}">Cardinalities</li>
        <ul>
        <xsl:for-each select="os:cardinality">
            <li style="{$txt-style}"><xsl:text>cardinality of </xsl:text>
                <xsl:if test="(@object='role')">
                    <xsl:call-template name="RoleRef">
                        <xsl:with-param name="id"><xsl:value-of select="@id" /></xsl:with-param>
                    </xsl:call-template>
                </xsl:if>
                <xsl:if test="(@object='group')">
                    <xsl:call-template name="GrSpecRef">
                        <xsl:with-param name="id"><xsl:value-of select="@id" /></xsl:with-param>
                    </xsl:call-template>
                </xsl:if>
                <xsl:text> is </xsl:text>
                <xsl:call-template name="show-cardinality">
                    <xsl:with-param name="min"><xsl:value-of select="@min" /></xsl:with-param>
                    <xsl:with-param name="max"><xsl:value-of select="@max" /></xsl:with-param>
                </xsl:call-template>
            </li>
        </xsl:for-each>
        </ul>
    </xsl:if>
    
    
    <xsl:if test="count(os:compatibility)>0">
        <xsl:if test="$soExtends='false'">
            <li style="{$txt-style}">Local compatibilities</li>
        </xsl:if>
        <xsl:if test="$soExtends='true'">
            <li style="{$txt-style}"><xsl:text>Compatibilities from </xsl:text>
                <xsl:call-template name="GrSpecRef">
                    <xsl:with-param name="id"><xsl:value-of select="../@id"/></xsl:with-param>
                </xsl:call-template>
            </li>
        </xsl:if>

        <ul>
        <xsl:for-each select="os:compatibility">
            <xsl:if test="($soExtends='true' and @extends-subgroups='true') or $soExtends='false'">
                <li style="{$txt-style}"><xsl:text>role </xsl:text>
                    <xsl:call-template name="RoleRef">
                        <xsl:with-param name="id"><xsl:value-of select="@from" /></xsl:with-param>
                    </xsl:call-template>
                    <xsl:text> is </xsl:text>
                    <xsl:value-of select="@scope"/>
                    <xsl:text> compatible with </xsl:text>
                    <xsl:call-template name="RoleRef">
                        <xsl:with-param name="id"><xsl:value-of select="@to"/></xsl:with-param>
                    </xsl:call-template>
                    <xsl:if test="@symmetric='true'">
                        <xsl:text> and vice versa </xsl:text>
                    </xsl:if>
                    <xsl:if test="$soExtends='false'">
                        <xsl:text> (this compatibility </xsl:text>
                        <xsl:if test="@extends-subgroups='true'">
                            <xsl:text>is extended to subgroups</xsl:text>
                        </xsl:if>
                        <xsl:if test="@extends-subgroups='false'">
                            <xsl:text>is not extended to subgroups</xsl:text>
                        </xsl:if>
                        <xsl:text>)</xsl:text>
                    </xsl:if>
                </li>
            </xsl:if>
        </xsl:for-each>
        </ul>
    </xsl:if>
    </ul>

    <xsl:apply-templates select="../../../os:constrain-formation" >
        <xsl:with-param name="soExtends">true</xsl:with-param>
    </xsl:apply-templates>
    </span>
</xsl:template>


<xsl:template match="os:cardinality">
    <xsl:param name="id" />

    <xsl:if test="(@id=$id)">
                <xsl:call-template name="show-cardinality">
                    <xsl:with-param name="min"><xsl:value-of select="@min" /></xsl:with-param>
                    <xsl:with-param name="max"><xsl:value-of select="@max" /></xsl:with-param>
                </xsl:call-template>
    </xsl:if>
</xsl:template>


<xsl:template name="show-cardinality">
    <xsl:param name="max" select="0" />
    <xsl:param name="min" select="*" />
    <xsl:text>(</xsl:text>
    <xsl:if test="string-length($min) != 0">
        <xsl:value-of select="$min"/>
    </xsl:if>
    <xsl:if test="string-length($min) = 0">
        <xsl:text>0</xsl:text>
    </xsl:if>
    <xsl:text>,</xsl:text>
    <xsl:if test="string-length($max) != 0">
        <xsl:value-of select="$max"/>
    </xsl:if>
    <xsl:if test="string-length($max) = 0">
        <xsl:text>*</xsl:text>
    </xsl:if>
    <xsl:text>)</xsl:text>
</xsl:template>


<!--


     FS
   
  
-->

<xsl:template match="os:functional-specification">
    <hr/><h2 style="{$h2-style}">Functional Specification</h2>
  
    <xsl:apply-templates select="os:properties" />
    <xsl:apply-templates select="os:scheme" />

</xsl:template>

<xsl:template match="os:scheme">
    <xsl:param name="sid" select="@id"/>
    <h3 style="{$h3-style}">Scheme <i><xsl:value-of select="@id"/></i></h3>
    <a id="{$sid}" />

    <xsl:apply-templates select="os:properties" />
    <xsl:if test="count(.//os:goal)>0">
        <p/>
        <table border="0" cellspacing="3" cellpadding="6">
        <tr style="{$trh-style}"> 
        <th valign="top" style="{$th-style}">goal</th>
        <th valign="top" style="{$th-style}">mission</th>
        <th valign="top" style="{$th-style}">type</th>
        <th valign="top" style="{$th-style}"># agents that should satisfy</th>
        <th valign="top" style="{$th-style}">ttf</th>
        <th valign="top" style="{$th-style}">description</th>
        <th valign="top" style="{$th-style}">arguments</th>
        <th valign="top" style="{$th-style}">plan</th>   
        </tr>
        <xsl:apply-templates select="os:goal|os:goal/os:plan//os:goal" />
        </table>
    </xsl:if>

    <!--  <p/>Plans
    <ul><xsl:apply-templates select="os:goal/os:plan" /> </ul> -->

    <p/>
    <span style="{$txt-style}">
    <b>Missions</b>
    </span>
    <ul><xsl:apply-templates select="os:mission" /></ul>
</xsl:template>

<xsl:template match="os:goal">
    <tr style="{$tr-style}">
        <td style="{$td-style}">
        <xsl:call-template name="GoalRef">
            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
        </xsl:call-template>
        </td>
        
        <td style="{$td-style}">
        <xsl:variable name="gId" select='@id'/>
        <!-- xsl:value-of select="//os:mission[os:goal/@id = $gId]/@id"/ -->
        <xsl:for-each select="//os:mission[os:goal/@id = $gId]">
             <xsl:value-of select="@id"/>
             <xsl:if test="not(position()=last())"><br/></xsl:if>
        </xsl:for-each>
        </td>

        <td style="{$td-style}">
        <xsl:value-of select="@type"/> 
        </td>

        <td style="{$td-style}">
            <xsl:if test="string-length(@min)=0">
                <xsl:text>all committed</xsl:text>
            </xsl:if>
            <xsl:if test="string-length(@min)>0">
                <xsl:value-of select="@min"/>
            </xsl:if>
        </td>
        
        <td style="{$td-style}">
        <xsl:value-of select="@ttf"/> 
        </td>

        <td style="{$td-style}">
        <xsl:value-of select="@ds"/> 
        </td>
        
        <td style="{$td-style}">
        <xsl:if test="count(os:argument)>0" >
          <xsl:text>{</xsl:text>
          <xsl:for-each select="os:argument">
                <xsl:value-of select="@id" />
                <xsl:if test="string-length(@value)>0">
                    <xsl:text>=</xsl:text>
                    <xsl:value-of select="@value" />
                </xsl:if>
                <xsl:if test="not(position()=last())"><xsl:text>, </xsl:text></xsl:if>
          </xsl:for-each>
          <xsl:text>}</xsl:text>
        </xsl:if>
        </td>
         
        <td style="{$td-style}">
        <xsl:if test="count(os:plan)>0">
          <xsl:apply-templates select="os:plan" />
        </xsl:if>
        <xsl:if test="count(os:depends-on)>0">
          depends on 
          <xsl:for-each select="os:depends-on">
                <xsl:call-template name="GoalRef">
                    <xsl:with-param name="id"><xsl:value-of select="@goal"/></xsl:with-param>
                </xsl:call-template>
                <xsl:if test="not(position()=last())"><xsl:text>, </xsl:text></xsl:if>
          </xsl:for-each>
        </xsl:if>
        </td>
        
    </tr>
</xsl:template>


<xsl:template match="os:plan">
        <xsl:variable name="pOp" select='@operator'/> 
        <xsl:if test="string-length(@successRate)>0">
            <sub>(<xsl:value-of select="@successRate"/>)&#160;</sub>
        </xsl:if>
        <xsl:for-each select="os:goal">
            <xsl:call-template name="GoalRef">
                <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
            </xsl:call-template>
            <xsl:if test="not(position()=last())">
                <xsl:if test="$pOp='sequence'">, </xsl:if>
                <xsl:if test="$pOp='choice'"> | </xsl:if>
                <xsl:if test="$pOp='parallel'"> || </xsl:if>
            </xsl:if>
        </xsl:for-each>
        <xsl:apply-templates select="property" />
    
        <!-- xsl:call-template name="GoalRef">
            <xsl:with-param name="id"><xsl:value-of select="../@id"/></xsl:with-param>
        </xsl:call-template

        <xsl:variable name="pOp" select='@operator'/> 
        <xsl:text> = </xsl:text>
        <xsl:if test="string-length(@success-rate) > 0 and @success-rate != 1.0" >
                <sub>(<xsl:value-of select="@success-rate"/>)&#160;</sub>
        </xsl:if>
        
        <xsl:for-each select="os:goal">
            <xsl:call-template name="GoalRef">
                <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
            </xsl:call-template>
            <xsl:if test="not(position()=last())">
                <xsl:if test="$pOp='sequence'"><xsl:text>, </xsl:text></xsl:if>
                <xsl:if test="$pOp='choice'"><xsl:text> | </xsl:text></xsl:if>
                <xsl:if test="$pOp='parallel'"><xsl:text> || </xsl:text></xsl:if>
            </xsl:if>
        </xsl:for-each>
        <xsl:text>.</xsl:text>
        
        <xsl:if test="count(os:goal/os:plan) > 0">
            <ul>
               <xsl:apply-templates select="os:goal/os:plan" />
            </ul>
        </xsl:if>
        <xsl:apply-templates select="os:properties" />
    </li>
    -->    
</xsl:template>



<xsl:template match="os:mission">
    <li>
     <span style="{$txt-style}">
         <xsl:call-template name="MissionRef">
             <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
         </xsl:call-template>
         <xsl:text>: goals = {</xsl:text>
             <xsl:for-each select="os:goal">
                     <xsl:call-template name="GoalRef">
                         <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
                     </xsl:call-template>
                 <xsl:if test="not(position()=last())"><xsl:text>, </xsl:text></xsl:if>
             </xsl:for-each>
         <xsl:text>}</xsl:text>
         <xsl:if test="@min">
                 <xsl:text>, cardinality = </xsl:text>
                 <xsl:call-template name="show-cardinality">
                     <xsl:with-param name="min"><xsl:value-of select="@min" /></xsl:with-param>
                     <xsl:with-param name="max"><xsl:value-of select="@max" /></xsl:with-param>
                 </xsl:call-template>
         </xsl:if>
    
         <xsl:variable name="misId" select='@id'/> 
         <xsl:if test="count(os:preferred)>0">
             <br/><xsl:text>preferred = </xsl:text>
             <xsl:for-each select="os:preferred">
                 <xsl:call-template name="MissionRef">
                     <xsl:with-param name="id"><xsl:value-of select="@mission"/></xsl:with-param>
                 </xsl:call-template>
                 <xsl:if test="not(position()=last())"><xsl:text>, </xsl:text></xsl:if>
             </xsl:for-each>
         </xsl:if>
     </span>
    </li>
</xsl:template>




<xsl:template match="os:normative-specification">
    <hr/><h2 style="{$h2-style}">Normative Specification</h2>
    <xsl:apply-templates select="properties" />
    
    <table border="0" cellspacing="3" cellpadding="6">
    <tr style="{$trh-style}"> 
    <th style="{$th-style}">id</th>
    <th style="{$th-style}">condition</th>
    <th style="{$th-style}">role</th> 
    <th style="{$th-style}">relation</th>
    <th style="{$th-style}">mission</th>
    <th style="{$th-style}">time constraint</th>
    <th style="{$th-style}">properties</th>
    </tr>
    <xsl:apply-templates select="os:norm" >
       <xsl:sort select="@id" />
    </xsl:apply-templates>
    </table>
</xsl:template>

<xsl:template match="os:norm">
    <tr style="{$tr-style}">
        <td style="{$td-style}">
        <xsl:value-of select="@id"/>
        </td>
        <td style="{$td-style}">
        <xsl:value-of select="@condition"/>
        </td>
        <td style="{$td-style}"> 
        <xsl:call-template name="RoleRef">
            <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
        </xsl:call-template>
        </td>
        
        <td style="{$td-style}">
        <i><xsl:value-of select="@type"/> </i>
        </td>
        
        <td style="{$td-style}">
        <xsl:call-template name="MissionRef">
            <xsl:with-param name="id"><xsl:value-of select="@mission"/></xsl:with-param>
        </xsl:call-template>
        </td>
        
        <td style="{$td-style}">
        <xsl:value-of select="@time-constraint"/>
        </td>
        
        <td style="{$td-style}">
        <xsl:value-of select="@referee"/>
        </td>
        
        <td style="{$td-style}">
        <xsl:apply-templates select="os:properties" />
        </td>
    </tr>
</xsl:template>

<xsl:template match="os:properties">
      <xsl:if test="count(os:property)>0">
        <span style="{$txt-style}">
        <br/><b>Properties</b>:
        <ul>
        <xsl:apply-templates select="os:property" >
            <xsl:sort select="@id" /> 
        </xsl:apply-templates > 
        </ul>
        </span>
      </xsl:if>
</xsl:template>

<xsl:template match="os:property">
      <li style="{$txt-style}"><xsl:value-of select="@id"/> = <xsl:value-of select="@value"/></li>
</xsl:template>

</xsl:stylesheet>

