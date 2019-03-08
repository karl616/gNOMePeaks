# gNOMePeaks
Peak calling in whole-ganome NOMe data

## Installation

### Dependencies

* R
* R packages:
  * HiddenMarkov (modified version supplied)
  * snow
  * parallel
* java
* gawk or mawk (macOS comes with nawk ==> use brew to install gawk)

### create jar-file from source

A part of gNOMePeaks is written in java. This has to be compiled and packed into an executable jar file

```
bash java/createJar.sh
```

### Modify paths in main script: gNOMeHMM.sh

Normally, the script doesn't need to be changed, but if java, Rscript or awk cannot be found in the PATH, this can be configured at the head of the script together with the temporary folder to use. If the awk variant mawk is installed, its use is beneficial to the computation time.

### Install modified version of HiddenMarkov (optional)

gNOMePeaks utilizes the R package HiddenMarkov for hidden Markov models. This package has problems when the coverage becomes too high. Then the probabilities becomes small and if this goes beyond the precision of your configuration, the segmentation step will break. We supply a modified version of the package that calculates log-likelihood values completely in log-space and so allows smaller values. The modification is not done for the complete package, but only for the binomial model used by gNOMePeaks. It can be installed with the following command:

```
R CMD INSTALL ...PATH.../gNOMePeaks/thirdParty/HiddenMarkov.mod
```

## Input format

gNOMePeaks works with methylation calls in Bis-SNP format. The first line of the this format contains track information and will be ignored. In essence it is enough to provide a tab-separated file with six columns, and an arbitrary or empty first line:

- chr, chromosome
- start, start of Cytosine in GC context
- end, end of Cytosine in GC context (start+1)
- methylation ratio, double value scaled from 0 to 100
- coverage, number of reads covering the position
- strand, +/-

Example:

```
track name=sampleFile type=bedDetail description="GCH methylation level" visibility=3
1       10481   10482   0.00    1       -
1       10482   10483   0.00    4       +
1       10485   10486   0.00    1       -
1       10486   10487   0.00    4       +
1       10490   10491   0.00    6       +
1       10494   10495   0.00    5       +
1       10520   10521   0.00    5       -
1       10521   10522   12.50   8       +
1       10526   10527   0.00    8       +
1       10551   10552   0.00    4       -
1       10552   10553   12.50   8       +
```

## Output format

The gNOMePeaks output table contains the following fields:

- chr, chromosome
- start, position of the first GC included in the region
- stop, position of the last GC included in the region
- p.value, Fisher's test p-value comparing methylation in region to the surrounding background
- avg(fg\_cov), average read coverage in region
- avg.meth, average GC-methylation in region
- avg(fg+bg\_cov), average coverage in region and surrounding background
- coverage\_group, The p-value adjustment is stratified over these coverage groups
- q.value, Adjusted p-value

## Advanced usage

### Cluster support 

Several steps of gNOMePeaks can be executed simultaneously. The current version of gNOMePeaks comes with support for GridEngine. This can be activated by uncommenting the PARALLELIZEPREFIX line at the top of the main script. This is an experimental option and might need modification to work on your cluster.
