lmatMult <- function(a,b){
 # matrix multiplication of matrices in log-space
 M<-matrix(data=0, nrow=dim(a)[1], ncol=dim(b)[2])
 for (i in 1:dim(M)[1]){
  for (j in 1:dim(M)[2]){
   M[i,j]<-lsum(a[i,]+b[,j])
  }
 }
 M
}

