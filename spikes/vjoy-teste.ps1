<#
.SYNOPSIS
  Teste descartável do vJoy: move os eixos do volante virtual sem o celular.

.DESCRIPTION
  Usa o dispositivo do vJoy assim:
    X = direção    Y = acelerador    Z = freio

  Modos:
    status     Só mostra se o vJoy e os eixos estão disponíveis. Não mexe em nada.
    eixo       Move um único eixo devagar, para atribuí-lo nas opções do jogo.
    varredura  Gira a direção de um lado para o outro e alterna acelerador e freio.
    teclado    Controla com o jogo em foco: J/L direção, I acelerador, K freio,
               Espaço centraliza. As teclas também chegam ao jogo; evite usá-las lá.

  Ctrl+C encerra: solta os pedais, centraliza a direção e libera o dispositivo.

.EXAMPLE
  powershell -ExecutionPolicy Bypass -File .\vjoy-teste.ps1 -Modo eixo -Eixo X
#>
param(
  [ValidateSet('status', 'eixo', 'varredura', 'teclado')]
  [string]$Modo = 'status',

  [ValidateSet('X', 'Y', 'Z')]
  [string]$Eixo = 'X',

  [ValidateRange(1, 16)]
  [int]$Dispositivo = 1,

  # 0 = roda até Ctrl+C
  [ValidateRange(0, 86400)]
  [int]$Segundos = 0,

  [string]$DllPath = (Join-Path $env:ProgramFiles 'vJoy\x64\vJoyInterface.dll')
)

$ErrorActionPreference = 'Stop'

if (-not [Environment]::Is64BitProcess) { throw 'Rode no PowerShell de 64 bits.' }
if (-not (Test-Path -LiteralPath $DllPath)) {
  throw "vJoyInterface.dll não encontrada em '$DllPath'. Informe o caminho com -DllPath."
}

Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;
public static class VJoy {
  [DllImport("kernel32.dll", SetLastError = true, CharSet = CharSet.Unicode)]
  public static extern IntPtr LoadLibrary(string path);
  [DllImport("vJoyInterface.dll")] public static extern bool vJoyEnabled();
  [DllImport("vJoyInterface.dll")] public static extern int GetVJDStatus(uint id);
  [DllImport("vJoyInterface.dll")] public static extern bool AcquireVJD(uint id);
  [DllImport("vJoyInterface.dll")] public static extern void RelinquishVJD(uint id);
  [DllImport("vJoyInterface.dll")] public static extern bool GetVJDAxisExist(uint id, uint axis);
  [DllImport("vJoyInterface.dll")] public static extern bool GetVJDAxisMax(uint id, uint axis, ref int max);
  [DllImport("vJoyInterface.dll")] public static extern bool SetAxis(int value, uint id, uint axis);
  [DllImport("user32.dll")] public static extern short GetAsyncKeyState(int key);
}
'@

if ([VJoy]::LoadLibrary($DllPath) -eq [IntPtr]::Zero) {
  throw "Não foi possível carregar '$DllPath' (erro $([Runtime.InteropServices.Marshal]::GetLastWin32Error()))."
}

$id = [uint32]$Dispositivo
$usos = @{ X = 0x30; Y = 0x31; Z = 0x32 }
$nomesEixo = @{ X = 'direção'; Y = 'acelerador'; Z = 'freio' }
$nomesStatus = @{
  0 = 'já em uso por este programa'
  1 = 'livre'
  2 = 'ocupado por outro programa'
  3 = 'não existe (habilite-o no Configure vJoy)'
  4 = 'desconhecido'
}

if (-not [VJoy]::vJoyEnabled()) { throw 'O driver do vJoy não está ativo.' }

$status = [VJoy]::GetVJDStatus($id)
Write-Host "vJoy ativo. Dispositivo ${Dispositivo}: $($nomesStatus[$status])."

$min = 1
$max = @{}
foreach ($e in 'X', 'Y', 'Z') {
  if ([VJoy]::GetVJDAxisExist($id, $usos[$e])) {
    $m = 0
    if (-not [VJoy]::GetVJDAxisMax($id, $usos[$e], [ref]$m) -or $m -le $min) { $m = 32768 }
    $max[$e] = $m
    Write-Host ("  Eixo {0} ({1}): ok, 1 a {2}" -f $e, $nomesEixo[$e], $m)
  } else {
    Write-Host ("  Eixo {0} ({1}): NÃO habilitado" -f $e, $nomesEixo[$e])
  }
}

if ($Modo -eq 'status') { return }

if ($status -eq 2) { throw 'Outro programa está usando o dispositivo (por exemplo, o vJoy Feeder). Feche-o e tente de novo.' }
if ($status -ge 3) { throw 'Dispositivo indisponível. Habilite-o no Configure vJoy.' }
if (-not $max.ContainsKey('X')) { throw 'Habilite ao menos o eixo X no Configure vJoy.' }
if (($Modo -eq 'eixo') -and -not $max.ContainsKey($Eixo)) { throw "O eixo $Eixo não está habilitado no Configure vJoy." }
if (-not [VJoy]::AcquireVJD($id)) { throw 'Não foi possível assumir o dispositivo.' }

$script:falhas = 0
function Set-Fracao([string]$nome, [double]$f) {
  if (-not $max.ContainsKey($nome)) { return }
  $f = [Math]::Max(0.0, [Math]::Min(1.0, $f))
  $valor = [int][Math]::Round($min + $f * ($max[$nome] - $min))
  if (-not [VJoy]::SetAxis($valor, $id, $usos[$nome])) { $script:falhas++ }
}

$estado = @{ direcao = 0.0; acel = 0.0; freio = 0.0 }   # direção -1..1; pedais 0..1
function Enviar {
  Set-Fracao 'X' (($estado.direcao + 1.0) / 2.0)
  Set-Fracao 'Y' $estado.acel
  Set-Fracao 'Z' $estado.freio
}
function Tecla([int]$vk) { ([VJoy]::GetAsyncKeyState($vk) -band 0x8000) -ne 0 }

switch ($Modo) {
  'eixo'      { Write-Host "Movendo só o eixo $Eixo ($($nomesEixo[$Eixo])). Atribua-o no jogo agora." }
  'varredura' { Write-Host 'Direção vai de um lado ao outro a cada 6 s; acelerador e freio se alternam.' }
  'teclado'   { Write-Host 'Com o jogo em foco: J/L direção, I acelerador, K freio, Espaço centraliza.' }
}
Write-Host 'Ctrl+C para encerrar.'

$relogio = [Diagnostics.Stopwatch]::StartNew()
$anterior = 0.0
$proximaLinha = 0.0
try {
  while ($Segundos -eq 0 -or $relogio.Elapsed.TotalSeconds -lt $Segundos) {
    $t = $relogio.Elapsed.TotalSeconds
    $dt = [Math]::Min(0.1, $t - $anterior)
    $anterior = $t

    switch ($Modo) {
      'eixo' {
        $onda = [Math]::Sin(2 * [Math]::PI * $t / 4)
        $estado.direcao = 0.0; $estado.acel = 0.0; $estado.freio = 0.0
        if ($Eixo -eq 'X') { $estado.direcao = $onda }
        elseif ($Eixo -eq 'Y') { $estado.acel = ($onda + 1) / 2 }
        else { $estado.freio = ($onda + 1) / 2 }
      }
      'varredura' {
        $estado.direcao = 0.8 * [Math]::Sin(2 * [Math]::PI * $t / 6)
        $p = [Math]::Sin(2 * [Math]::PI * $t / 3)
        $estado.acel = [Math]::Max(0.0, $p)
        $estado.freio = [Math]::Max(0.0, -$p)
      }
      'teclado' {
        $alvo = 0.0
        if (Tecla 0x4A) { $alvo -= 1.0 }   # J
        if (Tecla 0x4C) { $alvo += 1.0 }   # L
        if (Tecla 0x20) { $estado.direcao = 0.0 }
        elseif ($alvo -ne 0.0) {
          $estado.direcao = [Math]::Max(-1.0, [Math]::Min(1.0, $estado.direcao + $alvo * 1.2 * $dt))
        }
        if (Tecla 0x49) { $estado.acel = [Math]::Min(1.0, $estado.acel + 2.0 * $dt) }    # I
        else { $estado.acel = [Math]::Max(0.0, $estado.acel - 4.0 * $dt) }
        if (Tecla 0x4B) { $estado.freio = [Math]::Min(1.0, $estado.freio + 2.0 * $dt) } # K
        else { $estado.freio = [Math]::Max(0.0, $estado.freio - 4.0 * $dt) }
      }
    }

    Enviar

    if ($t -ge $proximaLinha) {
      $proximaLinha = $t + 0.1
      [Console]::Write(("`rDireção {0,5:N0}%   Acelerador {1,4:N0}%   Freio {2,4:N0}%   " -f ($estado.direcao * 100), ($estado.acel * 100), ($estado.freio * 100)))
    }
    Start-Sleep -Milliseconds 10
  }
}
finally {
  $estado.direcao = 0.0; $estado.acel = 0.0; $estado.freio = 0.0
  Enviar
  [VJoy]::RelinquishVJD($id)
  Write-Host ''
  if ($script:falhas -gt 0) { Write-Host "Atenção: $($script:falhas) envios ao vJoy falharam." }
  Write-Host 'Pedais soltos, direção centralizada e dispositivo liberado.'
}
