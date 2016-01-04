library(mclust)

findShrinkage= function(sh,md,data){
 s<-summary(Mclust(t(data), G=1:9, prior = priorControl(shrinkage=sh)),parameters=T)
 low=sum(s$mean<md)
 if(low<1 && length(s$mean)>2){
  return(1e6+sh)
 }
 return((sum(s$mean<md)-1)*100000+sh)
}

args <- commandArgs(trailingOnly = T)
outputFile <- as.character(args[1])


f <- file("stdin")
open(f)
data<-as.numeric(readLines(f))
close(f)

cat(paste(length(data),"\n",sep=""), file=stderr())

data.downsampled<-sample(data,10000)

shrinkage<-optimize(findShrinkage,md=median(data),data=data.downsampled,lower=0.01,upper=150,tol=0.1)$minimum

model<-Mclust(t(data.downsampled),G=1:9, prior = priorControl(shrinkage=shrinkage))

#write.table(c(predict(model,t(data))$classification,recursive=T), "",sep='\t', col.names=F, quote=F, row.names=F)
#flush(stdout())

#cat(c(predict(model,t(data))$classification,recursive=T), sep='\n')
#flush(stdout())

pred<- predict(model,t(data))




grp <- c(pred$classification,recursive=T)

if(length(grp) != length(data)){
 #sometimes the prediction fails. The observed case was in conection to extreme values, for which the p-value was zero for all states.
 bug <- which(sapply(pred$classification,length)==0)
 grp <- seq_along(data)
 grp[-bug] <- c(pred$classification,recursive=T)
 grp[bug] <- max(grp[-bug])
}


cat(paste(length(grp),"\n",sep=""),file=stderr())

#save(shrinkage,data,model,pred, file=paste(outputFile,".debug.rda",sep=""))

write.table(grp, outputFile, sep='\t', col.names=F, quote=F, row.names=F)
