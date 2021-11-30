# TODOs

### Improving CMA-ES Optimizer

Current implementation of CMA-ES optimizer is a (dirty) modified version of the CMAES optimizer found in Hipparchus 2.0
library, which is, in turn, based on Apache Commons Math 3.6.1. This CMAES implementation is stable and robust but very
slow. The implementation is unnecessarily complex, made it difficult to work with Island model of the OptimK library.

One alternative is to reuse code published
on [cmap.polytechinique.fr](http://www.cmap.polytechnique.fr/~nikolaus.hansen/cmaes_inmatlab.html), this code, at least
the Java version is however quite unstable.

~~### Named Engine~~

~~Engine should have a string name associated, this could be useful when debugging multi-island model~~

### Provide Surrogate and SurrogateProblem interface

Should investigate into surrogate model

