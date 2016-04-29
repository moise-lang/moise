<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
     xmlns:os='http://moise.sourceforge.net/os'
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="os.xsl" />

<xsl:param name="goalId"/>

<xsl:template match="/">
    <xsl:for-each select="//os:goal[@id=$goalId]" >
    	<xsl:if test="position()=1">
	    	<xsl:apply-templates select="." />
    	</xsl:if>
    </xsl:for-each>
</xsl:template>



<xsl:template match="os:goal">
      <h2><xsl:value-of select="@id" /> (<xsl:value-of select="@type" /> goal)</h2>
      <hr />
      
      <xsl:value-of select="@ds" />

      <p/>
      Number of agents that should satisfy this goal to be considered globally satisfied: <xsl:value-of select="@min"/>
      
      <xsl:apply-templates select="os:properties" />
      
      <xsl:if test="count(os:argument)>0" >
	      <p/>Arguments:
	      <ul>
	      <xsl:for-each select="os:argument">
				<li>
				<xsl:value-of select="@id" />
	            <xsl:if test="string-length(@value)>0">
					<xsl:text> default value is </xsl:text>
					<xsl:value-of select="@value" />
            	</xsl:if>
	            </li>
	      </xsl:for-each>
	      </ul>
      </xsl:if>
      
      <xsl:if test="count(os:plan)>0" >
	      <p/>Plan:
	      <ul>
	      <xsl:apply-templates select="os:plan"/>
	      </ul>
      </xsl:if>
      
      <xsl:if test="../@operator != ''" >
	      <p/>In plan:
	      <ul>
	      <xsl:apply-templates select=".."/>
	      </ul>
      </xsl:if>
</xsl:template>


</xsl:stylesheet>

