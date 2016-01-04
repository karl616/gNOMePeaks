#!/usr/bin/env Rscript

library(parallel)

args<-commandArgs(TRUE)

bufferSize<- as.numeric(args[1])
numberOfCores<- as.numeric(args[2])
#adjustSize<- bufferSize
p.cutoff<- as.numeric(args[3])
downSampleTo<-as.numeric(args[4])

write(paste("p.cutoff:",p.cutoff),stderr())

f <- file("stdin")
open(f)
count<-0
used<-0
while(length(lines<-readLines(f,n=bufferSize))>0){
# read table is increadibly slow... do a small hack
 write(paste(count,used,"\r"),stderr(),append=TRUE)
 count<-count+length(lines)
 dataStr<-matrix(c(strsplit(lines,'\t'),recursive=T),ncol=9,byrow=TRUE)
 allData<-matrix(as.integer(dataStr[,4:9]),ncol=6)
 nSites<-allData[,c(3,6)]
 data<-allData[,c(1:2,4:5)]
 rm(allData)

 if(!is.na(downSampleTo)){
  #downsample targets? perhaps this should be done at the same time for all...
  cidx=1:2
  ridx<-which(rowSums(data[,cidx])>downSampleTo*nSites[,1])
  if(length(ridx)>1){ #TODO: fix the single loci cases.. rowSums needs a matrix... give it to it...
   data[ridx,cidx]<-cbind(round(downSampleTo*nSites[ridx,1]*data[ridx,cidx[1]]/rowSums(data[ridx,cidx])),
   round(downSampleTo*nSites[ridx,1]*data[ridx,cidx[2]]/rowSums(data[ridx,cidx])))
  }
  #downsample background
  cidx=3:4
  ridx<-which(rowSums(data[,cidx])>downSampleTo*nSites[,2])
  if(length(ridx)>1){
   data[ridx,cidx]<-cbind(round(downSampleTo*nSites[ridx,2]*data[ridx,cidx[1]]/rowSums(data[ridx,cidx])),
   round(downSampleTo*nSites[ridx,2]*data[ridx,cidx[2]]/rowSums(data[ridx,cidx])))
  }
  #TODO: coverage should be saved, so that it can be printed in the output...
 }
 
 p<-c(mclapply(seq_along(lines),function(i){
  fisher.test(matrix(c(data[i,],recursive=T),ncol=2),alternative="g")$p.value
 },mc.cores=numberOfCores),recursive=T)
 toUse<- p<p.cutoff
 used<- used+ sum(toUse)
 if(sum(toUse)>0){
  #prints chr, start, stop, p-value, avg coverage, average methylation, average cov in fg +bg
  write.table(data.frame(dataStr[toUse,1:3], p[toUse],rowSums(data[toUse,1:2])/nSites[toUse,1],data[toUse,1]/rowSums(data[toUse,1:2]), rowSums(data[toUse,])/rowSums(nSites[toUse,])), "",sep='\t', col.names=F, quote=F, row.names=F)
  flush(stdout())
 }
}
close(f)


