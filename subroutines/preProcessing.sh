#$ -cwd
#$ -S /bin/bash
#$ -V
#$ -j y
#$ -l mem_free=4G
#$ -o /dev/null

inputFile=$1
mergeGpC=$2
covCutoff=$3
TMPWD=$4
output=$5

mkdir -p $TMPWD/tmp

if [[ "$mergeGpC" == "T" ]]
then
 join -1 1 -2 1 -a 1 -a 2 <(awk '$6=="+" {print $1"_"($2-1),$6,$1,($2-1),$4,$5}' $inputFile |sort -T $TMPWD/tmp -k1b,1) <(awk '$6=="-" {print $1"_"$2,$6,$1,$2,$4,$5}' $inputFile |sort -T $TMPWD/tmp -k1b,1) | 
 awk -vOFS='\t' 'NF==11 {print $3,$4,$4+2,($5*$6+$10*$11)/($6+$11),$6+$11; next} {print $3,$4,$4+2,$5,$6}' -
else
 cut -f 1-5 $inputFile
fi |
awk -vcovCutoff=$covCutoff '$5>covCutoff' - |
sort -T $TMPWD/tmp -k1,1 -k2,2g -k3,3g - > $output

