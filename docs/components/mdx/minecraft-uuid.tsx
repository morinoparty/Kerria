/**
 * Minecraft の UUID（Mojang が発行する UUID v4）の例を、プレイヤーの頭アイコン付きで表示する
 *
 * Kerria が採番する UUID v7（アカウントID など）と見分けやすくするために使う。
 */

// 例として表示するプレイヤーと、その Minecraft UUID
const KNOWN_PLAYERS: Record<string, string> = {
    Notch: "069a79f4-44e9-4726-a5be-fca90e38aaf5",
    jeb_: "853c80ef-3c37-49fd-aa49-938b674adae6",
};

interface MinecraftUUIDProps {
    // 表示するプレイヤー名（KNOWN_PLAYERS のキー）
    name?: string;
}

export const MinecraftUUID = ({ name = "Notch" }: MinecraftUUIDProps) => {
    const uuid = KNOWN_PLAYERS[name] ?? KNOWN_PLAYERS.Notch;
    return (
        <span className="inline-flex items-center gap-2 whitespace-nowrap">
            {/* mcheads の API からプレイヤーの頭の画像を取得する */}
            <img
                src={`https://api.mcheads.org/head/${name}/64`}
                alt={`${name} の頭`}
                width={24}
                height={24}
                className="m-0 inline-block rounded-sm"
                style={{ imageRendering: "pixelated" }}
            />
            <span>
                <code>{uuid}</code>
                <br />
                <small>({name})</small>
            </span>
        </span>
    );
};
