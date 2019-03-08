#!/bin/bash

name="gNOMeHMM.sh"

# Local configuration
#AWK=mawk
export AWK=gawk
Rscript=Rscript
java=java

## Path for temporary files
export tmpFolder=$TMPDIR

## uncomment to activate SGE support
#PARALLELIZEPREFIX="qsub -sync y -cwd -j y -o tmp.log -V -N gNOMePeaks -l mem_free=16G -t 1-"

## Set SCRIPTFOLDER to the folder containing the scripts if you want to
##  add this software to your path. Default behaviour is to identify the
##  installation folder from the path of the executable
#SCRIPTFOLDER=

# Run time parameters (DON'T CHANGE)

## default number of background base pairs up- and down-stream
FLANKSIZE=4000

## default minimum coverage
covCutoff=5

## default maximum distance between GpCs
maxGpCDist=150

## default is to merge the two strands. (F for not doing this)
mergeGpC="T"

## default is to cluster based on coverage before calculating FDR
clusterForFDR="T"

## how to randomize. default is to only shuffle methylation values. 
randMethod="standard"

## defining the region to be used for background
getBackground="nomePeaksFilteredRegionEff"

#TODO: remove
## legacy parameter for down-sampling
downSampleTo=""

## default maximum coverage. Sites with larger coverage are reduced to this value
downSiteTo=25000

## number of states in the hidden Markov model
nrOfStates=2

## number of cores to use in in R
RCORES=3

## number of lines to read into memory when calculating Fisher's exact test
RBATCH=100000

## if set to 1, the temporary files are kept. default is to remove them.
keepTmp=0



printHelp() {
  >&2 echo -e "Usage: $name <options>"
  >&2 echo -e " Mandatory:"
  >&2 echo -e "  -i INPUTFILE\tA file containing GCH calls in Bis-SNP bed format"
  >&2 echo -e "  -o OUTPUTFILE\tThe file into which the results are written"
  >&2 echo -e " Optional:"
  >&2 echo -e "  -S DIR\tfolder containing the main script. Must be set if submitted to the cluster"
  >&2 echo -e "  -m\t\tIf set, the calls on different strands will be kept separate. (default: not set)"
  >&2 echo -e "  -c INT\tAn integer used to filter out low coverage sites. This is done after merging."
  >&2 echo -e "        \t (default: $covCutoff)"
  >&2 echo -e "  -r\t\tIf set, coverage and methylation levels are shuffled together. (default: not set)"
  >&2 echo -e "  -f INT\tSize up- and downstream of each peak to use as background (default: $FLANKSIZE bp)"
  >&2 echo -e "  -u\t\tFix the size used for background calculation. By default, the peak"
  >&2 echo -e "    \t\t regions annotated by the HMM are excluded from the background."
  >&2 echo -e "  -D INT\tdown-sample sites in order to avoid precision problems (default: $downSiteTo)"
  >&2 echo -e "  -s\t\tdo not cluster before calculation of FDR"
  >&2 echo -e "  -t DIR\tlocation to were temporary files can be written"
  >&2 echo -e "  -T\t\tif set, temporary files will be kept"
  >&2 echo -e "  -N INT\tnumber of states in the hidden markov model (default: $nrOfStates, must be 2, 3 or 4)"
  >&2 echo -e "  -n INT\tnumber of states to consider as peaks in the hidden markov model (default: $nrOfStates - 1= $nrOfStatesToUse"
  >&2 echo -e ""
}


while getopts ":i:o:S:mc:L:f:t:D:G:N:n:sTurh" opt; do
  case $opt in
    h) printHelp; exit 0 ;;
    i) INPUTFILE="$OPTARG" ;;
    o) OUTPUT="$OPTARG" ;;
    S) SCRIPTFOLDER="${OPTARG%/}" ;;
    m) mergeGpC="F" ;;
    c) covCutoff="$OPTARG" ;;
    r) randMethod="withCount" ;;
    f) FLANKSIZE="$OPTARG" ;;
    u) getBackground="nomePeaksFilteredRegion" ;;
    D) downSiteTo="$OPTARG" ;;
    G) maxGpCDist="$OPTARG" ;;
    N) nrOfStates="$OPTARG" ;;
    n) nrOfStatesToUse="$OPTARG" ;;
    s) clusterForFDR="F" ;;
    t) tmpFolder="$OPTARG" ;;
    T) keepTmp=1 ;;
    \?)
      >&2 echo -e "\nERROR: `date` ($name): INVALID OPTION: -$OPTARG\n";
      printHelp
      exit 1
      ;;
    :)
      >&2 echo -e "\nERROR: `date` ($name): OPTION -$OPTARG REQUIRES AN ARGUMENT\n"
      printHelp
      exit 1
      ;;
  esac
done

if [[ -z "$INPUTFILE" ]] || [[ -z "$OUTPUT" ]]; then
  >&2 echo ""
  >&2 echo "ERROR: `date` ($name): ARGUMENTS -i AND -o ARE MANDATORY"
  >&2 echo ""
  printHelp
  exit 1
fi

if [[ "$nrOfStates" -lt 2 ]] || [[ "$nrOfStates" -gt 4 ]]; then
  >&2 echo ""
  >&2 echo "ERROR: `date` ($name): Unsupported number of states for the hidden markov model: $nrOfStates"
  >&2 echo ""
  printHelp
  exit 2
fi

if [[ -z "$nrOfStatesToUse" ]]; then
  nrOfStatesToUse=$(( $nrOfStates -1 ))
fi

if [[ "$nrOfStatesToUse" -lt 1 ]] || [[ "$nrOfStatesToUse" -ge "$nrOfStates" ]]; then
  >&2 echo ""
  >&2 echo "ERROR: `date` ($name): Can't use this number of states from a hidden markov model with $nrOfStates states: $nrOfStatesToUse"
  >&2 echo ""
  print Help
  exit 3
fi

>&2 echo "LOGG `date` ($name): START"

if [[ -z "$SCRIPTFOLDER" ]]; then
  pushd `dirname $0` > /dev/null
    SCRIPTFOLDER=`pwd -P`
  popd > /dev/null
fi

set -o pipefail

TMPWD=`mktemp --tmpdir=$tmpFolder -d gnomePeaks.XXXXXXXXXX`
mkdir -p $TMPWD/{sep,fdr/group}


BISUTILS=$SCRIPTFOLDER/java/bisUtils.jar

fdrGroup=$TMPWD/fdr/group
SITES=$TMPWD/sites.bed
PEAKSP=$TMPWD/peak_withP.bed

SHUFFLEDPDIST=$TMPWD/shuf_peak.pDist


# allow both gzipped and uncompressed files to pass through
#TODO: move the downsampling of the sites to the preProcessing step.. It makes more sense there
if [[ "$INPUTFILE" == *gz ]]; then
  zcat $INPUTFILE
else
  cat $INPUTFILE
fi \
  | tail -n +2 \
  | $AWK -vds=$downSiteTo -vOFS='\t' '$5>ds {$5=ds} {print}' \
  | bash $SCRIPTFOLDER/subroutines/gNOMePeaks_splitOnChr.sh - $TMPWD/sep/raw_ 0 && rm $TMPWD/sep/raw_rest.bed

#postDecodeCol=`zcat $INPUTFILE |tail -n +2 - |$AWK '{print NF+2;exit}'`
postDecodeCol=7

# pre-processing = putative merge of GcPs and coverage filter
# prepare pre-processing for parallelization
for raw in $TMPWD/sep/raw*bed; do
  echo "bash $SCRIPTFOLDER/subroutines/preProcessing.sh $raw $mergeGpC $covCutoff $TMPWD `dirname $raw`/`basename $raw |sed s/raw_//` $AWK"
done > $TMPWD/job20.preProcessing.txt


if [[ "$PARALLELIZEPREFIX" == qsub* ]]; then
  $PARALLELIZEPREFIX$(cat $TMPWD/job20.preProcessing.txt |wc -l) $SCRIPTFOLDER/subroutines/executeLine.sh -j $TMPWD/job20.preProcessing.txt || (>&2 echo "ERROR `date` ($name): parallelized pre-processing failed";exit 59)
else
  bash $TMPWD/job20.preProcessing.txt || (>&2 echo "ERROR `date` ($name): pre-processing failed";exit 59)
fi

rm $TMPWD/sep/raw_*.bed

#prepare parallelization of segmentation
for peak in $TMPWD/sep/*.bed; do
  echo "$Rscript --vanilla $SCRIPTFOLDER/subroutines/computeSegmentation.r ${peak} `dirname $peak`/1st_site_`basename ${peak/.bed/}` $SCRIPTFOLDER/subroutines/HMMLib.r $RCORES $randMethod $nrOfStates > ${peak/.bed/.Log} "
done > $TMPWD/job30.computeSegmentation.txt

if [[ "$PARALLELIZEPREFIX" == qsub* ]]; then
  $PARALLELIZEPREFIX$(cat $TMPWD/job30.computeSegmentation.txt |wc -l) $SCRIPTFOLDER/subroutines/executeLine.sh -j $TMPWD/job30.computeSegmentation.txt || (>&2 echo "ERROR `date` ($name): parallelized segmentation failed";exit 63)
else
  bash $TMPWD/job30.computeSegmentation.txt || (>&2 echo "ERROR `date` ($name): segmentation failed";exit 63)
fi

#extract peak regions
#TODO: add a filter for minimum number of sites per peak?
#TODO: add error check??
for bed in $TMPWD/sep/1st_site*{perm,real}.bed; do
  peak=${bed/site/peak}
  $AWK -vFS=' ' -vOFS='\t' -vPDC=$postDecodeCol -vmaxDist=$maxGpCDist -vbreakPoint=$(( $nrOfStates - $nrOfStatesToUse )) '
    curChr!=$1 {
      if(state>breakPoint){
        print curChr,start,lastEnd;
      } 
      lastEnd=0;
      curChr=$1;
      state=-1;
    }

    state!=$PDC || $2-lastEnd>maxDist {
      if(state>breakPoint){
        print curChr,start,lastEnd;
      } 
      state=$PDC;
      start=$2;
    }
 
    {
      lastEnd=$3
    }

    END{
      if(state>breakPoint){
        print curChr,start,lastEnd;
      }
    }
  ' $bed > $peak
done

#prepare parallelization of the p-value calculation
for peak in $TMPWD/sep/1st_peak_*{perm,real}.bed; do
  site=${peak/1st_peak_/1st_site_}
  echo "$java -Xmx4G -jar $BISUTILS $getBackground $site $peak $peak $FLANKSIZE | $Rscript $SCRIPTFOLDER/subroutines/NOME_fisherExactTest.R $RBATCH $RCORES 1 $downSampleTo > ${peak/.bed/.withP.bed}" 
done > $TMPWD/job40.calculateP.txt

if [[ "$PARALLELIZEPREFIX" == qsub* ]]; then
  $PARALLELIZEPREFIX$(cat $TMPWD/job40.calculateP.txt |wc -l) $SCRIPTFOLDER/subroutines/executeLine.sh -j $TMPWD/job40.calculateP.txt || (>&2 echo "ERROR `date` ($name): parallelized calculation of fisher test failed";exit 71)
else
  bash $TMPWD/job40.calculateP.txt || (>&2 echo "ERROR `date` ($name): calculation of fisher test failed";exit 71)
fi

#merge real data
sort -k1,1 -k2,2n -k3,3n -m $TMPWD/sep/1st_peak_*real.withP.bed > $PEAKSP || (>&2 echo "ERROR `date` ($name): merge data failed";exit 73)

#merge background data 
#sort here or later...
cut -f 4,7 $TMPWD/sep/1st_peak_*perm.withP.bed |sort -k1,1g > $SHUFFLEDPDIST || (>&2 echo "ERROR `date` ($name): merge of background failed";exit 75)



if [[ "$clusterForFDR" == "T" ]]; then
  cat <(cut -f 7 $PEAKSP) <(cut -f 2 $SHUFFLEDPDIST) \
    | $Rscript $SCRIPTFOLDER/subroutines/NOME_mixtureModel.R $TMPWD/classification.csv || (>&2 echo "ERROR `date` ($name): coverage stratification failed";exit 78)

  if [[ $(cat $TMPWD/classification.csv |wc -l) -eq $(cat $PEAKSP $SHUFFLEDPDIST |wc -l) ]]; then
    cat $TMPWD/classification.csv \
      | head -n $(wc -l $PEAKSP| cut -f 1 -d ' ' ) \
      | paste $PEAKSP - \
      | $AWK -vfolder=$fdrGroup '{print > folder"/g_"$NF".fg.raw"}'
    cat $TMPWD/classification.csv \
      | tail -n $(wc -l $SHUFFLEDPDIST| cut -f 1 -d ' ' ) \
      | paste $SHUFFLEDPDIST - \
      | $AWK -vfolder=$fdrGroup '{print $1 > folder"/g_"$NF".bg.raw"}'
  else
    >&2 echo "LOGG `date` ($name): Warning: clustering for FDR calculations failed. Will continue without clustering"
    cut -f 1 $SHUFFLEDPDIST > $fdrGroup/g_1.bg.raw
    $AWK -vOFS='\t' '{print $0,1}' $PEAKSP > $fdrGroup/g_1.fg.raw
  fi
else
  cut -f 1 $SHUFFLEDPDIST > $fdrGroup/g_1.bg.raw
  $AWK -vOFS='\t' '{print $0,1}' $PEAKSP > $fdrGroup/g_1.fg.raw
fi

#calculate grouped FDR
for foreground in $fdrGroup/*.fg.raw; do
  background=${foreground%.fg.raw}.bg.raw
  $java -Xmx32G -jar $BISUTILS gNOMe_addFDR $foreground $background $(wc -l $background |cut -f 1 -d ' ') 2 > ${foreground%.raw}.fdr 2> /dev/null || (>&2 echo "ERROR `date` ($name): FDR calculation failed";exit 87)
done

sort -k1,1 -k2,2n -k3,3n  $fdrGroup/*.fg.fdr \
  | $AWK -vOFS='\t' 'BEGIN{print "chr","start","stop","p.value","avg(fg_cov)","avg.meth","avg(fg+bg_cov)","coverage_group","q.value"} {print}'> $OUTPUT




if [ $keepTmp == 0 ]; then
  rm -rf $TMPWD
else
  >&2 echo "LOGG `date` ($name): Temporary files kept in folder: $TMPWD" 
fi

>&2 echo "LOGG `date` ($name): DONE"
