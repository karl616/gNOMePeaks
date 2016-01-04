
library(HiddenMarkov)
library(snow)

loadAndSmoothBedFile <- function(filename){
  bed=read.table(filename,stringsAsFactors=F)
  names(bed)=c("chr","pos","type","r","count")
  bed$meth=round(bed$r/100*bed$count)
  #compute easy smoothingbw.ucv(x, nb = 1000
  #bed$smooth=ksmooth(bed$pos,bed$r,bandwidth=20,x.points=bed$pos)$y
  #bed$smooth=ksmooth(bed$pos,bed$r,bandwidth=bw.nrd(bed$r),x.points=bed$pos)$y
  #better use a density estimate with cross validated bandwidth
  #bed$density=density(bed$r,bw=bw.nrd(bed$r))$y
  #plot(bed$pos[1:100],bed$r[1:100])
  #lines(bed$pos[1:100],bed$smooth[1:100],col=2)
  return(bed)
}

constructHMMFromBed <- function(bed,states=2,nrOfCpus=1){
  
  #init HMM parameters
  
  # vector of "fixed & known" number of Bernoulli trials
  pn <- list(size=bed$count)
  
  #depending on model complexity initialize HMM differently
  if(states==2){
    delta=c(1,0)
    Pi = matrix(c(0.8,0.2,0.3,0.7),byrow=T,nrow=2)
    x <- dthmm(NULL, Pi, delta, "binom", list(prob=c(0.1, 0.6)), pn,
               discrete=TRUE,nonstat=T)
  }
  if(states==3){
    delta=c(1,0,0)
    Pi = matrix(c(0.8,0.1,0.1,0.1,0.8,0.1,0.1,0.1,0.8),byrow=T,nrow=3)
    x <- dthmm(NULL, Pi, delta, "binom", list(prob=c(0.1,0.3, 0.6)), pn,
               discrete=TRUE,nonstat=T)
  }
  if(states==4){
    delta=c(1,0,0,0)
    Pi = matrix(c(0.85,0.05,0.05,0.05,0.05,0.85,0.05,0.05,0.05,0.05,0.85,0.05,0.05,0.05,0.05,0.85),byrow=T,nrow=4)
    x <- dthmm(NULL, Pi, delta, "binom", list(prob=c(0.1,0.3,0.5, 0.7)), pn,
               discrete=TRUE,nonstat=T)
  }
  
  #assign the vector of positive events (methylated C's)
  x$x=bed$meth
  c1<- makeCluster(nrOfCpus,"MPI")
  # use above parameter values as initial values and start do EM
#  y <- BaumWelch(x,bwcontrol(maxiter=1000,tol=0.0001),SNOWcluster=c1)
#  y <- BaumWelch(x,bwcontrol(maxiter=1000,tol=5e-3),SNOWcluster=c1)
  y <- BaumWelch(x,bwcontrol(maxiter=1000,tol=5e-3))
  stopCluster(c1)
  
  return(y)
}
#update the bed table with predictins from the hmm object
annotateBedFile <- function(hmm,bed){
  
  #bed$viterbi=Viterbi(hmm)
  bed$postdecod=apply(hmm$u,1,which.max)
  #by default use the 2nd state posterior
  ff=forwardback(hmm$x,hmm$Pi,hmm$delta,hmm$distn,hmm$pm,hmm$pn)
  bed$posterior1=ff$logalpha[,1]+ff$logbeta[,1]
  bed$posterior2=ff$logalpha[,2]+ff$logbeta[,2]
  if(ncol(ff$logbeta)>=3){
    bed$posterior3=ff$logalpha[,3]+ff$logbeta[,3]
  }
  return(bed)
  
}

modelSelectionScores <- function(hmm){
  #compute AICc and BIC for the HMM 
  #k = free parameters of the model
  k=length(hmm$Pi)-nrow(hmm$Pi)  + length(hmm$pm)
  #n number of datapoints
  n=length(hmm$x)
  aicc=(2*k - 2* hmm$LL) + 2*k*(k+1)/(n-k-1)
  bic=-2*hmm$LL + k*log(n)
  return(list(c(AICc=aicc,BIC=bic)))
}

computePeaksFromBed <- function(bed){
  #we use the column postdecod for "peak" calling
  bed$peak=rep("1",nrow(bed))
  for( i in nrow(bed)-1){
    bed$peak=paste()
  }
  #extract only lines with 12 or 21
  subset(bed,peak == "12" | peak == "21")
}

#for a given HMM object show a selected range x..y (vector positions) in a plot comparing 
#state assignments and 
visualComparison <- function(bed,x,y){
  #change plotting device
  par(mfrow=c(3,1))
  plot(bed$pos[x:y],bed$r[x:y]/100)
  # plot(bed$pos[x:y],bed$viterbi[x:y],type="l",lwd=3,col="red")
  plot(bed$pos[x:y],bed$postdecod[x:y],type="l",lwd=3,col="blue")
  plot(bed$pos[x:y],bed$posterior1[x:y],type="l",lwd=3,col="green")
  lines(bed$pos[x:y],bed$posterior2[x:y],lwd=3,col="blue")
  
  par(mfrow=c(1,1))
}

visualComparison2 <- function(bed2,bed3,x,y){
  #change plotting device
  par(mfrow=c(3,1))
  plot(bed2$pos[x:y],bed2$r[x:y]/100,ylab="methylation ratio")
  # plot(bed$pos[x:y],bed$viterbi[x:y],type="l",lwd=3,col="red")
  plot(bed2$pos[x:y],bed2$postdecod[x:y],type="l",lwd=3,col="blue",ylab="Post Decoding 2 state HMM")
  plot(bed2$pos[x:y],bed3$postdecod[x:y],type="l",lwd=3,col="blue",ylab="Post Decoding 3 state HMM")
  
  par(mfrow=c(1,1))
}
visualComparison3 <- function(bed2,bed3,bed4,x,y){
  #change plotting device
  par(mfrow=c(4,1))
  plot(bed2$pos[x:y],bed2$r[x:y]/100,ylab="methylation ratio")
  # plot(bed$pos[x:y],bed$viterbi[x:y],type="l",lwd=3,col="red")
  plot(bed2$pos[x:y],bed2$postdecod[x:y],type="l",lwd=3,col="blue",ylab="Post Decoding 2 state HMM")
  plot(bed2$pos[x:y],bed3$postdecod[x:y],type="l",lwd=3,col="blue",ylab="Post Decoding 3 state HMM")
  plot(bed2$pos[x:y],bed4$postdecod[x:y],type="l",lwd=3,col="blue",ylab="Post Decoding 4 state HMM")
  
  par(mfrow=c(1,1))
}
