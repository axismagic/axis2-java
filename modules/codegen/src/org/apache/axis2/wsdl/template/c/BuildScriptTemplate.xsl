<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>
<xsl:template match="interface">

<xsl:variable name="servicename"><xsl:value-of select="@servicename"/></xsl:variable>
<xsl:variable name="option"><xsl:value-of select="@option"/></xsl:variable>
<xsl:variable name="outputlocation"><xsl:value-of select="@outputlocation"/></xsl:variable>
<xsl:variable name="targetsourcelocation"><xsl:value-of select="@targetsourcelocation"/></xsl:variable>
<xsl:choose>
<xsl:when test="$option = 1">
gcc -g -shared -olib<xsl:value-of select="$servicename"/>.so -I $AXIS2C_HOME/include/axis2-1.3.0/ -I<xsl:value-of select="$targetsourcelocation"/> -L$AXIS2C_HOME/lib \
    -laxutil \
    -laxis2_axiom \
    -laxis2_engine \
    -laxis2_parser \
    -lpthread \
    -laxis2_http_sender \
    -laxis2_http_receiver \
    -laxis2_libxml2 \
    *.c <xsl:value-of select="@targetsourcelocation"/>/*.c
</xsl:when>
<xsl:otherwise>
gcc -g -shared -olib<xsl:value-of select="$servicename"/>.so -I $AXIS2C_HOME/include/axis2-1.3.0/  -L$AXIS2C_HOME/lib \
    -laxutil \
    -laxis2_axiom \
    -laxis2_engine \
    -laxis2_parser \
    -lpthread \
    -laxis2_http_sender \
    -laxis2_http_receiver \
    -laxis2_libxml2 \
    *.c 
</xsl:otherwise>
</xsl:choose>
</xsl:template>
</xsl:stylesheet>