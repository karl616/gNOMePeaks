      subroutine loop3(m, n, phi, S, eigenv, logalp, scalef, tau,
     1                 post, psi, psi0, psi1, tmp)
c     first loop (forward eqns) in forwardback.mmpp
      implicit none
      integer i, j, k, m, n
      double precision phi(m), sumphi, scalef(n), tau(n), post(m,m)
      double precision eigenv(m), S(m,m), logalp(n+1,m), psi(n,m,m)
      double precision psi0(m,m), psi1(m,m), tmp(m)
c     the above arrays occur in the subroutine call for
c     memory allocation reasons in non gfortran compilers
c     their contents are purely internal to this subroutine
      i = 1
      do while(i .le. n)
          call multi4(m, eigenv, post, psi0, tau(i))
          call multi3(m, m, m, S, psi0, psi1)
          j = 1
          do while(j .le. m)
              k = 1
              do while(k .le. m)
                  psi(i, j, k) = psi1(j, k)
                  k = k+1
              enddo
              j = j+1
          enddo
          call multi1(m, phi, psi1, tmp)
          sumphi=0.0
          j = 1
          do while(j .le. m)
              sumphi = sumphi + phi(j)
              j = j+1
          enddo
          scalef(i) = dlog(sumphi)
          j = 1
          do while(j .le. m)
              phi(j) = phi(j)/sumphi
              logalp(i+1,j) = dlog(phi(j))
              j = j+1
          enddo
          i = i+1
      enddo
      end


      subroutine loop4(m, n, phi, logbet, scalef, psi, psi1, tmp)
c     second loop (backward eqns) in forwardback.mmpp
      implicit none
      integer i, j, k, m, n
      double precision phi(m), scalef(n), logbet(n+1,m), psi(n,m,m)
      double precision logck, lscale, one, sumphi
      double precision psi1(m,m), tmp(m)
c     the above arrays occur in the subroutine call for
c     memory allocation reasons in non gfortran compilers
c     its contents are purely internal to this subroutine
      one = 1.00
      lscale = dlog(one*m)
      logck = 0.0
      i = n
      do while(i .ge. 1)
          j = 1
          do while(j .le. m)
              k = 1
              do while(k .le. m)
                  psi1(j, k) = psi(i, j, k)
                  k = k+1
              enddo
              j = j+1
          enddo
          call multi2(m, psi1, phi, tmp)
          logck = logck + scalef(i)
          sumphi=0.0
          j = 1
          do while(j .le. m)
              logbet(i, j) = dlog(phi(j)) + lscale - logck
              sumphi = sumphi + phi(j)
              j = j+1
          enddo
          j = 1
          do while(j .le. m)
              phi(j) = phi(j)/sumphi
              j = j+1
          enddo
          lscale = lscale + dlog(sumphi)
          i = i-1
      enddo
      end


      subroutine loop5(m, n, d, tau, scalef, diff, TT, exptau)
c     first loop in Estep.mmpp
c     d = eigenvalues
c     tau = interevent times
      implicit none
      integer i, j, k, m, n
      double precision d(m), scalef(n), tau(n), TT(n,m,m), diff(m,m)
      double precision exptau(m)
c     the above array occurs in the subroutine call for
c     memory allocation reasons in non gfortran compilers
c     its contents are purely internal to this subroutine
      i = 1
      do while(i .le. n)
          j = 1
          do while(j .le. m)
              exptau(j) = dexp(d(j)*tau(i))
              j = j+1
          enddo
          j = 1
          do while(j .le. m)
              k = 1
              do while(k .le. m)
                  TT(i,j,k) = exptau(j)-exptau(k)
                  if(j .eq. k) then
                      TT(i,j,k) = TT(i,j,k) + tau(i)*exptau(j)
                  endif
                  TT(i,j,k) = TT(i,j,k)/diff(j,k)/dexp(scalef(i))
                  k = k+1
              enddo
              j = j+1
          enddo
          i = i+1
      enddo
      end


      subroutine loop6(m, n, TT, S, Sinv, post0, alpha, beta, A,
     1                 pre, post, TTi, tmp, tmp0)
c     second loop in Estep.mmpp
      implicit none
      integer i, j, j1, k, m, n
      double precision TT(n,m,m), S(m,m), Sinv(m,m), post0(m,m)
      double precision alpha(n+1, m), beta(n+1, m), A(m,m), aa
      double precision pre(m,m), post(m,m), TTi(m,m), tmp(m), tmp0(m)
c     the above arrays occur in the subroutine call for
c     memory allocation reasons in non gfortran compilers
c     their contents are purely internal to this subroutine
      k = 1
      do while(k .le. m)
          call multi5(m, k, S, Sinv, pre)
          j = 1
          do while(j .le. m)
              call multi6(m, j, S, post0, post)
              i = 1
              do while(i .le. n)
                  call getrow(m, n+1, i, alpha, tmp)
                  call multi1(m, tmp, pre, tmp0)
                  call getmat(m, n, i, TT, TTi)
                  call multi1(m, tmp, TTi, tmp0)
                  call multi1(m, tmp, post, tmp0)
                  call getrow(m, n+1, i+1, beta, tmp0)
                  aa = 0
                  j1 = 1
                  do while(j1 .le. m)
                      aa = aa + tmp(j1)*tmp0(j1)
                      j1 = j1+1
                  enddo
                  A(k,j) = A(k,j)+aa
                  i = i+1
              enddo
              j = j+1
          enddo
          k = k+1
      enddo
      end

