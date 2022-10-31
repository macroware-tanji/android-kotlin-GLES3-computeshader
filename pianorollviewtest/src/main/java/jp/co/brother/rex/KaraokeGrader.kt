package jp.co.brother.rex

class KaraokeGrader {
    data class GradingSection(var head:Double, var tail:Double, var text:String)
    data class Mora(var time:Double,var duration: Double,var note:Int, var flag:Int)
    data class Criteria(var notes:Array<Mora>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Criteria

            if (!notes.contentEquals(other.notes)) return false

            return true
        }

        override fun hashCode(): Int {
            return notes.contentHashCode()
        }
    }
    data class GraderInfo(var vocalCount:Int,var timeRange:Array<Double>,var criteria:Array<Criteria> ,var gradingSections:Array<GradingSection>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GraderInfo

            if (vocalCount != other.vocalCount) return false
            if (!timeRange.contentEquals(other.timeRange)) return false
            if (!criteria.contentEquals(other.criteria)) return false
            if (!gradingSections.contentEquals(other.gradingSections)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = vocalCount
            result = 31 * result + timeRange.contentHashCode()
            result = 31 * result + criteria.contentHashCode()
            result = 31 * result + gradingSections.contentHashCode()
            return result
        }
    }
}