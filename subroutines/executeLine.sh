#!/bin/bash
#$ -S /bin/bash
#$ -N execLine
#$ -cwd
#$ -j y
#$ -V

name=exectuteLine.sh
export PATH=$PATH:/TL/deep-share/archive00/software/bin
set -o pipefail

printHelp(){
 echo -e "" >&2
 echo -e "when submitted as an array job with qsub, it will run the job on line n in the job file, where n corresponds to the job number" >&2
 echo -e "" >&2
 echo -e "Usage: $name <options>" >&2
 echo -e "" >&2
 echo -e "Mandatory options:" >&2
 echo -e " -j FILE\ta file with one command per line" >&2
 echo -e "" >&2
}

while getopts "j:h" opt
do
 case "$opt" in
  j) jobFile="$OPTARG" ;;
  h) printHelp; exit 1 ;;
 esac
done

if [[ -z "$jobFile" ]]
then
 echo -e "`date` ERROR ($name): All mandatory options must be set" >&2
 printHelp
 exit 1
fi

cmd="$(head -n ${SGE_TASK_ID} $jobFile |tail -n 1)"

echo -e "`date` LOGG ($name): CMD=$cmd"

eval "$cmd"
exit $?
