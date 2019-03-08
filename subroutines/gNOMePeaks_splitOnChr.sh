LOCALINPUTFILE=$1
LOCALOUTPUTPREFIX=$2
WITHHEADER=$3

#chromosome names containing an underscore will be put in the rest.bed file

mkdir -p `dirname $LOCALOUTPUTPREFIX`

if [[ $LOCALINPUTFILE == *.gz ]]
then
 zcat $LOCALINPUTFILE
else
 cat $LOCALINPUTFILE
fi | ${AWK:-gawk} -vWITHHEADER=$WITHHEADER -vFOLDER=$LOCALOUTPUTPREFIX '
NR==1 && WITHHEADER==1 {
 header=$0
 print header > FOLDER"rest.bed"
 next
}

!($1 in chrs) {
 if( $1 ~ /[^chrXY0-9]/){ 
  chrs[$1]=FOLDER"rest.bed"
 }else{
  chrs[$1]=FOLDER$1".bed"
  if(WITHHEADER==1){
   print header > chrs[$1]
  }
 }
}

{
 print $0 > chrs[$1]
}
'
