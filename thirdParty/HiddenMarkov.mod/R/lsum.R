lsum <- function(x){
   # sum of logged numbers
   xmax <- which.max(x)
   log1p(sum(exp(x[-xmax]-x[xmax])))+x[xmax]
} 
