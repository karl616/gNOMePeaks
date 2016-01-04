Mstep.binom <- function(x, cond, pm, pn){
    if (is.null(pn)) stop("Variable \"size\" must be specified in pn")
    prob <- as.numeric(matrix(x, nrow=1) %*% cond$u)/
                as.numeric(matrix(pn$size, nrow=1) %*% cond$u)
    prob <- as.numeric( lmatMult(matrix(log(x), nrow=1), cond$u) ) -
                as.numeric( lmatMult(matrix(log(pn$size), nrow=1), cond$u))

    return(list(prob=exp(prob)))
}

