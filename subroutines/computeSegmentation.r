#load params from CMD
args <- commandArgs(trailingOnly = TRUE)
filename=as.character(args[1])
outputPrefix=as.character(args[2])
hmmLibrary=as.character(args[3])
nrOfCpus=as.numeric(args[4])
randMethod<- as.character(args[5])
nrOfStates<- as.numeric(args[6])
if(length(args)<5){
 randMethod<-"standard"
}

print(filename)

#if( !exists(filename))
#{
#  stop("Parameters not specified.\nUsage: Rscript computeSegmentation.r bedfilename outPrefix")
#}
source(hmmLibrary)


#2state HMM
bed=loadAndSmoothBedFile(filename)
hmm2=constructHMMFromBed(bed, states=nrOfStates, nrOfCpus=nrOfCpus)
bed2=annotateBedFile(hmm2,bed)

bedperm=bed
#testing an alternative randomisation
if(randMethod=="withCount"){
 i<-sample.int(nrow(bedperm),nrow(bedperm))
 bedperm$r=bed$r[i]
 bedperm$count=bed$count[i]
 bedperm$meth=bed$meth[i]
}else{
 bedperm$r=sample(bedperm$r,nrow(bedperm))
 bedperm$meth=round(bedperm$r/100*bedperm$count)
}
hmmperm2=constructHMMFromBed(bedperm, states=nrOfStates, nrOfCpus=nrOfCpus)
bedperm2=annotateBedFile(hmmperm2,bedperm)


write.table(bed2,file=paste(outputPrefix,".real.bed",sep=""),quote=F,row.names=FALSE,col.names=FALSE, sep='\t')
write.table(bedperm2,file=paste(outputPrefix,".perm.bed",sep=""),quote=F,row.names=FALSE,col.names=FALSE, sep='\t')

