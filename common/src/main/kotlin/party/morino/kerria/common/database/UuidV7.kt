package party.morino.kerria.common.database

import java.security.SecureRandom
import java.util.UUID

/**
 * UUID v7（RFC 9562）を生成するユーティリティ
 *
 * 先頭 48 ビットが Unix 時刻（ミリ秒）になるため、作成順に並び、主キーとして使ったときにインデックスが断片化しにくい。
 * Kerria が採番する ID（アカウントID など）に使い、Minecraft のプレイヤー UUID（v4）と区別できるようにする。
 */
object UuidV7 {
    // 乱数部分の生成に使う（暗号論的に安全な乱数）
    private val random = SecureRandom()

    /**
     * 現在時刻を元に UUID v7 を生成する
     *
     * @param epochMillis 埋め込む Unix 時刻（ミリ秒）。テスト用に指定できる
     * @return 生成した UUID v7
     */
    fun generate(epochMillis: Long = System.currentTimeMillis()): UUID {
        // 上位 64 ビット: 時刻 48 ビット + バージョン 4 ビット（0111） + 乱数 12 ビット
        val timestamp = epochMillis and 0xFFFF_FFFF_FFFFL
        val randA = random.nextInt() and 0x0FFF
        val mostSignificant = (timestamp shl 16) or (0x7L shl 12) or randA.toLong()
        // 下位 64 ビット: バリアント 2 ビット（10） + 乱数 62 ビット
        val leastSignificant = (random.nextLong() and 0x3FFF_FFFF_FFFF_FFFFL) or Long.MIN_VALUE
        return UUID(mostSignificant, leastSignificant)
    }
}
