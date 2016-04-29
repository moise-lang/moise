<?xml version="1.0" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>


<!--
          * General propose functions *
-->

<xsl:template name="RoleRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <a href="role.xsl?roleId={$id}" ><xsl:value-of select="$id" /></a>
</xsl:template>


<xsl:template name="MissionRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <a href="mission.xsl?missionId={$id}" ><xsl:value-of select="$id" /></a>
</xsl:template>


<xsl:template name="GoalRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <a href="goal.xsl?goalId={$id}" ><xsl:value-of select="$id" /></a>
</xsl:template>


<xsl:template name="GrSpecRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <!--a href="groupSpec.xsl?grSpecId={$id}"><xsl:value-of select="$id" /></a-->
   <a href="../os#{$id}"><xsl:value-of select="$id" /></a>
</xsl:template>

<xsl:template name="SchemeSpecRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <!--a href="schemeSpec.xsl?schemeSpecId={$id}"><xsl:value-of select="$id" /></a -->
   <a href="../os#{$id}"><xsl:value-of select="$id" /></a>
</xsl:template>


<xsl:template name="AgentRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <a href="agent.xsl?agentId={$id}"><xsl:value-of select="$id" /></a>
</xsl:template>


<xsl:template name="GroupRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <a href="../group/{$id}"><xsl:value-of select="$id" /></a>
</xsl:template>

<xsl:template name="SchemeRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <a href="../scheme/{$id}" ><xsl:value-of select="$id" /></a>
</xsl:template>

</xsl:stylesheet>
