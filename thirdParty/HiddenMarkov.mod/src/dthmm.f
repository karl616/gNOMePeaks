      subroutine loop1(m, n, lphi, lprob, lPi, logalp, lscale, tmp)
c     first loop (forward eqns) in forwardback.dthmm
      implicit none
      integer i, j, m, n
      double precision lphi(m), lsumphi, lscale
      double precision lprob(n,m), lPi(m,m), logalp(n,m)
      double precision tmp(m)
c     the above array occurs in the subroutine call for
c     memory allocation reasons in non gfortran compilers
c     its contents are purely internal to this subroutine
      lscale = 0
      i = 1
      do while(i .le. n)
          if (i .gt. 1) call lmulti1(m, lphi, lPi, tmp)
          j = 2
          lphi(1) = lphi(1) + lprob(i, 1)
          lsumphi = lphi(1)
          do while(j .le. m)
              lphi(j) = lphi(j) + lprob(i, j)
              call lsum(lsumphi, lphi(j), lsumphi)
              j = j+1
          enddo
          j = 1
          do while(j .le. m)
              lphi(j) = lphi(j)-lsumphi
              j = j+1
          enddo
          lscale = lscale + lsumphi
          j = 1
          do while(j .le. m)
              logalp(i,j) = lphi(j) + lscale
              j = j+1
          enddo
          i = i+1
      enddo
      end


      subroutine loop2(m, n, lphi, lprob, lPi, logbet, lscale, tmp)
c     second loop (backward eqns) in forwardback.dthmm
      implicit none
      integer i, j, m, n
      double precision lphi(m), lsumphi, lscale
      double precision lprob(n,m), lPi(m,m), logbet(n,m)
      double precision tmp(m)
c     the above array occurs in the subroutine call for
c     memory allocation reasons in non gfortran compilers
c     its contents are purely internal to this subroutine
      i = n-1
      do while(i .ge. 1)
          j = 1
          do while(j .le. m)
              lphi(j) = lphi(j) + lprob(i+1, j)
              j = j+1
          enddo
          call lmulti2(m, lPi, lphi, tmp)
          j = 2
          logbet(i,1) = lphi(1) + lscale
          lsumphi = lphi(1)
          do while(j .le. m)
              logbet(i,j) = lphi(j) + lscale
              call lsum(lsumphi, lphi(j), lsumphi)
              j = j+1
          enddo
          j = 1
          do while(j .le. m)
              lphi(j) = lphi(j) - lsumphi
              j = j+1
          enddo
          lscale = lscale + lsumphi
          i = i-1
      enddo
      end
