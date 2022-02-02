package visdom.utils

import scala.reflect.ClassTag
import shapeless.syntax.std.tuple.productTupleOps


// scalastyle:off number.of.methods
object TupleUtils {
    implicit class EnrichedWithToTuple[A](elements: Seq[A]) {
        def toTuple1: Tuple1[A] = elements match {case Seq(a) => Tuple1(a)}
        def toTuple2: (A, A) = elements match {case Seq(a, b) => (a, b)}
        def toTuple3: (A, A, A) = elements match {case Seq(a, b, c) => (a, b, c)}
        def toTuple4: (A, A, A, A) = elements match {case Seq(a, b, c, d) => (a, b, c, d)}
        def toTuple5: (A, A, A, A, A) = elements match {case Seq(a, b, c, d, e) => (a, b, c, d, e)}
        def toTuple6: (A, A, A, A, A, A) = elements match {case Seq(a, b, c, d, e, f) => (a, b, c, d, e, f)}
        def toTuple7:  (A, A, A, A, A, A, A) = elements match {case Seq(a, b, c, d, e, f, g) => (a, b, c, d, e, f, g)}
        def toTuple8:  (A, A, A, A, A, A, A, A) =
            elements match {case Seq(a, b, c, d, e, f, g, h) => (a, b, c, d, e, f, g, h)}
        def toTuple9:  (A, A, A, A, A, A, A, A, A) =
            elements match {case Seq(a, b, c, d, e, f, g, h, i) => (a, b, c, d, e, f, g, h, i)}
        def toTuple10: (A, A, A, A, A, A, A, A, A, A) =
            elements match {case Seq(a, b, c, d, e, f, g, h, i, j) => (a, b, c, d, e, f, g, h, i, j)}
        def toTuple11: (A, A, A, A, A, A, A, A, A, A, A) =
            elements match {case Seq(a, b, c, d, e, f, g, h, i, j, k) => (a, b, c, d, e, f, g, h, i, j, k)}
        def toTuple12: (A, A, A, A, A, A, A, A, A, A, A, A) =
            elements match {case Seq(a, b, c, d, e, f, g, h, i, j, k, l) => (a, b, c, d, e, f, g, h, i, j, k, l)}
        def toTuple13: (A, A, A, A, A, A, A, A, A, A, A, A, A) =
            elements match {case Seq(a, b, c, d, e, f, g, h, i, j, k, l, m) => (a, b, c, d, e, f, g, h, i, j, k, l, m)}
        def toTuple14: (A, A, A, A, A, A, A, A, A, A, A, A, A, A) = elements match {
            case Seq(a, b, c, d, e, f, g, h, i, j, k, l, m, n) => (a, b, c, d, e, f, g, h, i, j, k, l, m, n)
        }
        def toTuple15: (A, A, A, A, A, A, A, A, A, A, A, A, A, A, A) = elements match {
            case Seq(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o) => (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o)
        }
        def toTuple16: (A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A) = elements match {
            case Seq(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p) => (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p)
        }
        def toTuple17: (A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A) = elements match {
            case Seq(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q) => (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q)
        }
    }

    def toTuple[A : ClassTag](items: Seq[Any]): Option[Tuple1[A]] = {
        items.headOption match {
            case Some(itemA: A) => Some(Tuple1(itemA))
            case _ => None
        }
    }

    def toTuple[A : ClassTag, B : ClassTag](items: Seq[Any]): Option[(A, B)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B](items.drop(1)) match {
                case Some(lastItem) => Some((firstItem._1, lastItem._1))
                case None => None
            }
            case None => None
        }
    }

    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag](items: Seq[Any]): Option[(A, B, C)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag](items: Seq[Any]): Option[(A, B, C, D)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C, D](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag,
                D : ClassTag, E : ClassTag](items: Seq[Any]): Option[(A, B, C, D, E)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C, D, E](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag,
                E : ClassTag, F : ClassTag](items: Seq[Any]): Option[(A, B, C, D, E, F)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C, D, E, F](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag,
                E : ClassTag, F : ClassTag, G : ClassTag](items: Seq[Any]): Option[(A, B, C, D, E, F, G)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C, D, E, F, G](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag, E : ClassTag,
                F : ClassTag, G : ClassTag, H : ClassTag](items: Seq[Any]): Option[(A, B, C, D, E, F, G, H)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C, D, E, F, G, H](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag, E : ClassTag, F : ClassTag,
                G : ClassTag, H : ClassTag, I : ClassTag](items: Seq[Any]): Option[(A, B, C, D, E, F, G, H, I)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C, D, E, F, G, H, I](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsMaxParameters))
    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag, E : ClassTag, F : ClassTag, G : ClassTag,
                H : ClassTag, I : ClassTag, J : ClassTag](items: Seq[Any]): Option[(A, B, C, D, E, F, G, H, I, J)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C, D, E, F, G, H, I, J](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsMaxParameters))
    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag, E : ClassTag,
                F : ClassTag, G : ClassTag, H : ClassTag, I : ClassTag, J : ClassTag,
                K : ClassTag](items: Seq[Any]): Option[(A, B, C, D, E, F, G, H, I, J, K)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C, D, E, F, G, H, I, J, K](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsMaxParameters))
    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag, E : ClassTag,
                F : ClassTag, G : ClassTag, H : ClassTag, I : ClassTag, J : ClassTag,
                K : ClassTag, L : ClassTag](items: Seq[Any]): Option[(A, B, C, D, E, F, G, H, I, J, K, L)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C, D, E, F, G, H, I, J, K, L](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsMaxParameters))
    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag, E : ClassTag, F : ClassTag,
                G : ClassTag, H : ClassTag, I : ClassTag, J : ClassTag, K : ClassTag, L : ClassTag,
                M : ClassTag](items: Seq[Any]): Option[(A, B, C, D, E, F, G, H, I, J, K, L, M)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C, D, E, F, G, H, I, J, K, L, M](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsMaxParameters))
    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag, E : ClassTag, F : ClassTag,
                G : ClassTag, H : ClassTag, I : ClassTag, J : ClassTag, K : ClassTag, L : ClassTag,
                M : ClassTag, N : ClassTag](items: Seq[Any]): Option[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C, D, E, F, G, H, I, J, K, L, M, N](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsMaxParameters))
    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag, E : ClassTag, F : ClassTag, G : ClassTag,
                H : ClassTag, I : ClassTag, J : ClassTag, K : ClassTag, L : ClassTag, M : ClassTag, N : ClassTag,
                O : ClassTag](items: Seq[Any]): Option[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C, D, E, F, G, H, I, J, K, L, M, N, O](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsMaxParameters))
    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag, E : ClassTag,
                F : ClassTag, G : ClassTag, H : ClassTag, I : ClassTag, J : ClassTag,
                K : ClassTag, L : ClassTag, M : ClassTag, N : ClassTag, O : ClassTag,
                P : ClassTag](items: Seq[Any]): Option[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsMaxParameters))
    def toTuple[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag, E : ClassTag, F : ClassTag,
                G : ClassTag, H : ClassTag, I : ClassTag, J : ClassTag, K : ClassTag, L : ClassTag,
                M : ClassTag, N : ClassTag, O : ClassTag, P : ClassTag, Q : ClassTag
               ](items: Seq[Any]): Option[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] = {
        toTuple[A](items) match {
            case Some(firstItem) => toTuple[B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](items.drop(1)) match {
                case Some(otherItems) => Some(firstItem._1 +: otherItems)
                case None => None
            }
            case None => None
        }
    }

    def toOption[A, B](
        values: (Any, Any),
        transformations: ((Any) => Option[A], (Any) => Option[B])
    ): Option[(A, B)] = {
        transformations._1(values._1) match {
            case Some(value1) => transformations._2(values._2) match {
                case Some(value2) => Some(value1, value2)
                case None => None
            }
            case None => None
        }
    }

    def toOption[A, B, C](
        values: (Any, Any, Any),
        transformations: ((Any) => Option[A], (Any) => Option[B], (Any) => Option[C])
    ): Option[(A, B, C)] = {
        transformations._1(values._1) match {
            case Some(value1) => transformations._2(values._2) match {
                case Some(value2) => transformations._3(values._3) match {
                    case Some(value3) => Some(value1, value2, value3)
                    case None => None
                }
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D](
        values: (Any, Any, Any, Any),
        transformations: ((Any) => Option[A], (Any) => Option[B], (Any) => Option[C], (Any) => Option[D])
    ): Option[(A, B, C, D)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (values._2, values._3, values._4),
                (transformations._2, transformations._3, transformations._4)
            ) match {
                case Some((value2, value3, value4)) =>
                    Some(value1, value2, value3, value4)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E](
        values: (Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A], (Any) => Option[B], (Any) => Option[C], (Any) => Option[D], (Any) => Option[E]
        )
    ): Option[(A, B, C, D, E)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (values._2, values._3, values._4, values._5),
                (transformations._2, transformations._3, transformations._4, transformations._5)
            ) match {
                case Some((value2, value3, value4, value5)) =>
                    Some(value1, value2, value3, value4, value5)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E, F](
        values: (Any, Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A], (Any) => Option[B], (Any) => Option[C],
            (Any) => Option[D], (Any) => Option[E], (Any) => Option[F]
            )
    ): Option[(A, B, C, D, E, F)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (values._2, values._3, values._4, values._5, values._6),
                (transformations._2, transformations._3, transformations._4, transformations._5, transformations._6)
            ) match {
                case Some((value2, value3, value4, value5, value6)) =>
                    Some(value1, value2, value3, value4, value5, value6)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E, F, G](
        values: (Any, Any, Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A], (Any) => Option[B], (Any) => Option[C], (Any) => Option[D],
            (Any) => Option[E], (Any) => Option[F], (Any) => Option[G]
        )
    ): Option[(A, B, C, D, E, F, G)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (values._2, values._3, values._4, values._5, values._6, values._7),
                (
                    transformations._2, transformations._3, transformations._4,
                    transformations._5, transformations._6, transformations._7
                )
            ) match {
                case Some((value2, value3, value4, value5, value6, value7)) =>
                    Some(value1, value2, value3, value4, value5, value6, value7)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E, F, G, H](
        values: (Any, Any, Any, Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A], (Any) => Option[B], (Any) => Option[C], (Any) => Option[D],
            (Any) => Option[E], (Any) => Option[F], (Any) => Option[G], (Any) => Option[H]
        )
    ): Option[(A, B, C, D, E, F, G, H)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (values._2, values._3, values._4, values._5, values._6, values._7, values._8),
                (
                    transformations._2, transformations._3, transformations._4, transformations._5,
                    transformations._6, transformations._7, transformations._8
                )
            ) match {
                case Some((value2, value3, value4, value5, value6, value7, value8)) =>
                    Some(value1, value2, value3, value4, value5, value6, value7, value8)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E, F, G, H, I](
        values: (Any, Any, Any, Any, Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A], (Any) => Option[B], (Any) => Option[C], (Any) => Option[D], (Any) => Option[E],
            (Any) => Option[F], (Any) => Option[G], (Any) => Option[H], (Any) => Option[I]
        )
    ): Option[(A, B, C, D, E, F, G, H, I)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (values._2, values._3, values._4, values._5, values._6, values._7, values._8, values._9),
                (
                    transformations._2, transformations._3, transformations._4, transformations._5,
                    transformations._6, transformations._7, transformations._8, transformations._9
                )
            ) match {
                case Some((value2, value3, value4, value5, value6, value7, value8, value9)) =>
                    Some(value1, value2, value3, value4, value5, value6, value7, value8, value9)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E, F, G, H, I, J](
        values: (Any, Any, Any, Any, Any, Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A], (Any) => Option[B], (Any) => Option[C], (Any) => Option[D], (Any) => Option[E],
            (Any) => Option[F], (Any) => Option[G], (Any) => Option[H], (Any) => Option[I], (Any) => Option[J]
        )
    ): Option[(A, B, C, D, E, F, G, H, I, J)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (values._2, values._3, values._4, values._5, values._6, values._7, values._8, values._9, values._10),
                (
                    transformations._2, transformations._3, transformations._4,
                    transformations._5, transformations._6, transformations._7,
                    transformations._8, transformations._9, transformations._10
                )
            ) match {
                case Some((value2, value3, value4, value5, value6, value7, value8, value9, value10)) =>
                    Some(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E, F, G, H, I, J, K](
        values: (Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A], (Any) => Option[B], (Any) => Option[C], (Any) => Option[D],
            (Any) => Option[E], (Any) => Option[F], (Any) => Option[G], (Any) => Option[H],
            (Any) => Option[I], (Any) => Option[J], (Any) => Option[K]
        )
    ): Option[(A, B, C, D, E, F, G, H, I, J, K)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (
                    values._2, values._3, values._4, values._5, values._6,
                    values._7, values._8, values._9, values._10, values._11
                ),
                (
                    transformations._2, transformations._3, transformations._4, transformations._5,
                    transformations._6, transformations._7, transformations._8, transformations._9,
                    transformations._10, transformations._11
                )
            ) match {
                case Some((value2, value3, value4, value5, value6, value7, value8, value9, value10, value11)) =>
                    Some(value1, value2, value3, value4, value5, value6, value7, value8, value9, value10, value11)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E, F, G, H, I, J, K, L](
        values: (Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A], (Any) => Option[B], (Any) => Option[C], (Any) => Option[D],
            (Any) => Option[E], (Any) => Option[F], (Any) => Option[G], (Any) => Option[H],
            (Any) => Option[I], (Any) => Option[J], (Any) => Option[K], (Any) => Option[L]
        )
    ): Option[(A, B, C, D, E, F, G, H, I, J, K, L)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (
                    values._2, values._3, values._4, values._5, values._6, values._7,
                    values._8, values._9, values._10, values._11, values._12
                ),
                (
                    transformations._2, transformations._3, transformations._4, transformations._5,
                    transformations._6, transformations._7, transformations._8, transformations._9,
                    transformations._10, transformations._11, transformations._12
                )
            ) match {
                case Some((
                    value2, value3, value4, value5, value6, value7,
                    value8, value9, value10, value11, value12
                )) =>
                    Some(
                        value1, value2, value3, value4, value5, value6,
                        value7, value8, value9, value10, value11, value12
                    )
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E, F, G, H, I, J, K, L, M](
        values: (Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A], (Any) => Option[B], (Any) => Option[C], (Any) => Option[D], (Any) => Option[E],
            (Any) => Option[F], (Any) => Option[G], (Any) => Option[H], (Any) => Option[I], (Any) => Option[J],
            (Any) => Option[K], (Any) => Option[L], (Any) => Option[M]
        )
    ): Option[(A, B, C, D, E, F, G, H, I, J, K, L, M)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (
                    values._2, values._3, values._4, values._5, values._6, values._7,
                    values._8, values._9, values._10, values._11, values._12, values._13
                ),
                (
                    transformations._2, transformations._3, transformations._4, transformations._5,
                    transformations._6, transformations._7, transformations._8, transformations._9,
                    transformations._10, transformations._11, transformations._12, transformations._13
                )
            ) match {
                case Some((
                    value2, value3, value4, value5, value6, value7,
                    value8, value9, value10, value11, value12, value13
                )) =>
                    Some(
                        value1, value2, value3, value4, value5, value6, value7,
                        value8, value9, value10, value11, value12, value13
                    )
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E, F, G, H, I, J, K, L, M, N](
        values: (Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A], (Any) => Option[B], (Any) => Option[C], (Any) => Option[D], (Any) => Option[E],
            (Any) => Option[F], (Any) => Option[G], (Any) => Option[H], (Any) => Option[I], (Any) => Option[J],
            (Any) => Option[K], (Any) => Option[L], (Any) => Option[M], (Any) => Option[N]
        )
    ): Option[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (
                    values._2, values._3, values._4, values._5, values._6, values._7, values._8,
                    values._9, values._10, values._11, values._12, values._13, values._14
                ),
                (
                    transformations._2, transformations._3, transformations._4, transformations._5,
                    transformations._6, transformations._7, transformations._8, transformations._9,
                    transformations._10, transformations._11, transformations._12, transformations._13,
                    transformations._14
                )
            ) match {
                case Some((
                    value2, value3, value4, value5, value6, value7, value8,
                    value9, value10, value11, value12, value13, value14
                )) =>
                    Some(
                        value1, value2, value3, value4, value5, value6, value7,
                        value8, value9, value10, value11, value12, value13, value14
                    )
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](
        values: (Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A], (Any) => Option[B], (Any) => Option[C], (Any) => Option[D], (Any) => Option[E],
            (Any) => Option[F], (Any) => Option[G], (Any) => Option[H], (Any) => Option[I], (Any) => Option[J],
            (Any) => Option[K], (Any) => Option[L], (Any) => Option[M], (Any) => Option[N], (Any) => Option[O]
        )
    ): Option[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (
                    values._2, values._3, values._4, values._5, values._6, values._7, values._8,
                    values._9, values._10, values._11, values._12, values._13, values._14, values._15
                ),
                (
                    transformations._2, transformations._3, transformations._4, transformations._5,
                    transformations._6, transformations._7, transformations._8, transformations._9,
                    transformations._10, transformations._11, transformations._12, transformations._13,
                    transformations._14, transformations._15
                )
            ) match {
                case Some((
                    value2, value3, value4, value5, value6, value7, value8,
                    value9, value10, value11, value12, value13, value14, value15
                )) =>
                    Some(
                        value1, value2, value3, value4, value5, value6, value7, value8,
                        value9, value10, value11, value12, value13, value14, value15
                    )
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](
        values: (Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A], (Any) => Option[B], (Any) => Option[C], (Any) => Option[D], (Any) => Option[E],
            (Any) => Option[F], (Any) => Option[G], (Any) => Option[H], (Any) => Option[I], (Any) => Option[J],
            (Any) => Option[K], (Any) => Option[L], (Any) => Option[M], (Any) => Option[N], (Any) => Option[O],
            (Any) => Option[P]
        )
    ): Option[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (
                    values._2, values._3, values._4, values._5, values._6, values._7, values._8, values._9,
                    values._10, values._11, values._12, values._13, values._14, values._15, values._16
                ),
                (
                    transformations._2, transformations._3, transformations._4, transformations._5,
                    transformations._6, transformations._7, transformations._8, transformations._9,
                    transformations._10, transformations._11, transformations._12, transformations._13,
                    transformations._14, transformations._15, transformations._16
                )
            ) match {
                case Some((
                    value2, value3, value4, value5, value6, value7, value8, value9,
                    value10, value11, value12, value13, value14, value15, value16
                )) =>
                    Some(
                        value1, value2, value3, value4, value5, value6, value7, value8,
                        value9, value10, value11, value12, value13, value14, value15, value16
                    )
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](
        values: (Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A], (Any) => Option[B], (Any) => Option[C], (Any) => Option[D], (Any) => Option[E],
            (Any) => Option[F], (Any) => Option[G], (Any) => Option[H], (Any) => Option[I], (Any) => Option[J],
            (Any) => Option[K], (Any) => Option[L], (Any) => Option[M], (Any) => Option[N], (Any) => Option[O],
            (Any) => Option[P], (Any) => Option[Q]
        )
    ): Option[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (
                    values._2, values._3, values._4, values._5, values._6, values._7, values._8, values._9,
                    values._10, values._11, values._12, values._13, values._14, values._15, values._16, values._17
                ),
                (
                    transformations._2, transformations._3, transformations._4, transformations._5,
                    transformations._6, transformations._7, transformations._8, transformations._9,
                    transformations._10, transformations._11, transformations._12, transformations._13,
                    transformations._14, transformations._15, transformations._16, transformations._17
                )
            ) match {
                case Some((
                    value2, value3, value4, value5, value6, value7, value8, value9,
                    value10, value11, value12, value13, value14, value15, value16, value17
                )) =>
                    Some(
                        value1, value2, value3, value4, value5, value6, value7, value8, value9,
                        value10, value11, value12, value13, value14, value15, value16, value17
                    )
                case None => None
            }
            case None => None
        }
    }
}
// scalastyle:on number.of.methods
