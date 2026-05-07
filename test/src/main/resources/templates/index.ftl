<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>🌍 测试页面</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        :root {
            --primary-color: #ff6b6b;
            --secondary-color: #4ecdc4;
            --accent-color: #ffe66d;
            --bg-light: #fff5f5;
            --bg-white: #ffffff;
            --text-primary: #2d3436;
            --text-secondary: #636e72;
            --border-color: #ffeaa7;
        }
        
        body {
            font-family: 'Comic Sans MS', 'Chalkboard', 'Comic Neue', cursive, sans-serif;
            line-height: 1.6;
            color: var(--text-primary);
            background: linear-gradient(135deg, #fff5f5 0%, #ffeaa7 50%, #81ecec 100%);
            min-height: 100vh;
        }
        
        .navbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 1rem 5%;
            background: rgba(255, 255, 255, 0.9);
            border-bottom: 3px dashed var(--primary-color);
            position: sticky;
            top: 0;
            z-index: 100;
        }
        
        .navbar .logo {
            font-size: 1.5rem;
            font-weight: 700;
            color: var(--primary-color);
            animation: bounce 2s infinite;
        }
        
        @keyframes bounce {
            0%, 100% { transform: translateY(0); }
            50% { transform: translateY(-5px); }
        }
        
        .navbar .nav-links {
            display: flex;
            gap: 1.5rem;
        }
        
        .navbar .nav-links a {
            text-decoration: none;
            color: var(--text-secondary);
            font-size: 0.95rem;
            font-weight: 600;
            transition: all 0.3s;
            padding: 0.5rem;
            border-radius: 0.5rem;
        }
        
        .navbar .nav-links a:hover {
            background: var(--accent-color);
            color: var(--primary-color);
            transform: rotate(5deg);
        }
        
        .navbar .actions {
            display: flex;
            align-items: center;
            gap: 0.75rem;
        }
        
        .btn {
            padding: 0.5rem 1rem;
            border-radius: 0.5rem;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.3s;
            display: inline-flex;
            align-items: center;
            font-size: 0.9rem;
        }
        
        .btn-primary {
            background: linear-gradient(135deg, var(--primary-color), #fd79a8);
            color: white;
            animation: pulse 1.5s infinite;
        }
        
        @keyframes pulse {
            0%, 100% { box-shadow: 0 0 0 0 rgba(255, 107, 107, 0.7); }
            50% { box-shadow: 0 0 0 10px rgba(255, 107, 107, 0); }
        }
        
        .btn-primary:hover {
            transform: scale(1.1);
        }
        
        .btn-secondary {
            background: transparent;
            color: var(--text-secondary);
            border: 2px dashed var(--secondary-color);
        }
        
        .btn-secondary:hover {
            background: var(--secondary-color);
            color: white;
        }
        
        .hero {
            padding: 4rem 5%;
            text-align: center;
            position: relative;
            overflow: hidden;
        }
        
        .hero::before {
            content: '🎉';
            position: absolute;
            top: 10%;
            left: 10%;
            font-size: 3rem;
            animation: float 3s infinite;
        }
        
        .hero::after {
            content: '🌟';
            position: absolute;
            bottom: 20%;
            right: 15%;
            font-size: 2.5rem;
            animation: float 4s infinite reverse;
        }
        
        @keyframes float {
            0%, 100% { transform: translateY(0) rotate(0deg); }
            50% { transform: translateY(-20px) rotate(10deg); }
        }
        
        .hero-content h1 {
            font-size: 3rem;
            font-weight: 800;
            color: var(--primary-color);
            margin-bottom: 1.5rem;
            text-shadow: 3px 3px 0 var(--accent-color);
            animation: rainbow 3s infinite;
        }
        
        @keyframes rainbow {
            0% { color: #ff6b6b; }
            25% { color: #ffa502; }
            50% { color: #2ed573; }
            75% { color: #1e90ff; }
            100% { color: #ff6b6b; }
        }
        
        .hero-content .subtitle {
            font-size: 1.25rem;
            color: var(--text-secondary);
            margin-bottom: 2rem;
            max-width: 600px;
            margin-left: auto;
            margin-right: auto;
            background: rgba(255, 255, 255, 0.7);
            padding: 1rem;
            border-radius: 1rem;
            border: 2px dotted var(--secondary-color);
        }
        
        .hero-content .actions {
            display: flex;
            gap: 1rem;
            justify-content: center;
            flex-wrap: wrap;
        }
        
        .hero-content .actions .btn {
            padding: 0.75rem 2rem;
            font-size: 1rem;
        }
        
        .stats {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 2rem;
            max-width: 800px;
            margin: 3rem auto;
            padding: 2rem;
            background: rgba(255, 255, 255, 0.8);
            border-radius: 1.5rem;
            border: 3px dashed var(--accent-color);
        }
        
        .stat-item {
            text-align: center;
            padding: 1rem;
            background: var(--bg-light);
            border-radius: 1rem;
            transition: all 0.3s;
        }
        
        .stat-item:hover {
            transform: scale(1.1) rotate(3deg);
            background: var(--accent-color);
        }
        
        .stat-item .number {
            font-size: 2rem;
            font-weight: 800;
            color: var(--primary-color);
            margin-bottom: 0.5rem;
        }
        
        .stat-item .label {
            font-size: 0.85rem;
            color: var(--text-secondary);
        }
        
        .features {
            padding: 4rem 5%;
        }
        
        .section-header {
            text-align: center;
            margin-bottom: 3rem;
        }
        
        .section-header h2 {
            font-size: 2rem;
            font-weight: 700;
            color: var(--text-primary);
            margin-bottom: 1rem;
            background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        
        .section-header p {
            font-size: 1rem;
            color: var(--text-secondary);
        }
        
        .features-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 2rem;
            max-width: 1000px;
            margin: 0 auto;
        }
        
        .feature-card {
            background: rgba(255, 255, 255, 0.9);
            padding: 2rem;
            border-radius: 1.5rem;
            border: 2px dashed var(--border-color);
            transition: all 0.3s;
            text-align: center;
        }
        
        .feature-card:hover {
            transform: translateY(-10px) rotate(-2deg);
            box-shadow: 0 20px 30px rgba(255, 107, 107, 0.2);
        }
        
        .feature-card .icon {
            font-size: 3rem;
            margin-bottom: 1rem;
            animation: spin 3s linear infinite;
        }
        
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        
        .feature-card h3 {
            font-size: 1.25rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 0.75rem;
        }
        
        .feature-card p {
            font-size: 0.9rem;
            color: var(--text-secondary);
        }
        
        .gallery {
            padding: 4rem 5%;
            background: rgba(255, 255, 255, 0.6);
        }
        
        .gallery-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 1rem;
            max-width: 600px;
            margin: 0 auto;
        }
        
        .gallery-item {
            aspect-ratio: 1;
            background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
            border-radius: 1rem;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 3rem;
            transition: all 0.3s;
            cursor: pointer;
        }
        
        .gallery-item:hover {
            transform: scale(1.1) rotate(10deg);
        }
        
        .joke-section {
            padding: 4rem 5%;
            text-align: center;
        }
        
        .joke-card {
            background: rgba(255, 255, 255, 0.9);
            padding: 2.5rem;
            border-radius: 2rem;
            max-width: 600px;
            margin: 0 auto;
            border: 3px dotted var(--primary-color);
        }
        
        .joke-card .emoji {
            font-size: 4.5rem;
            margin-bottom: 1.5rem;
            animation: bounce 1s infinite;
        }
        
        .joke-card .joke {
            font-size: 1.35rem;
            color: var(--text-primary);
            margin-bottom: 1.5rem;
            line-height: 1.9;
            font-weight: 500;
            white-space: pre-line;
        }
        
        .joke-card .btn {
            background: var(--accent-color);
            color: var(--text-primary);
        }
        
        .joke-card .btn:hover {
            background: var(--primary-color);
            color: white;
            transform: scale(1.05);
        }
        
        .footer {
            background: linear-gradient(135deg, var(--primary-color), #fd79a8);
            color: white;
            padding: 3rem 5%;
            text-align: center;
            border-top: 3px dashed white;
        }
        
        .footer-links {
            display: flex;
            justify-content: center;
            gap: 1.5rem;
            margin-bottom: 1.5rem;
            flex-wrap: wrap;
        }
        
        .footer-links a {
            color: rgba(255, 255, 255, 0.8);
            text-decoration: none;
            font-weight: 600;
            transition: all 0.3s;
        }
        
        .footer-links a:hover {
            color: white;
            transform: scale(1.2);
        }
        
        .footer p {
            opacity: 0.8;
            font-size: 0.9rem;
        }
        
        .confetti {
            position: fixed;
            width: 10px;
            height: 10px;
            background: var(--primary-color);
            border-radius: 50%;
            animation: confettiFall linear infinite;
            pointer-events: none;
        }
        
        @keyframes confettiFall {
            0% { transform: translateY(-10vh) rotate(0deg); opacity: 1; }
            100% { transform: translateY(100vh) rotate(720deg); opacity: 0; }
        }
        
        .demo-label {
            display: inline-block;
            background: rgba(16, 185, 129, 0.2);
            color: #059669;
            font-size: 0.7rem;
            font-weight: 600;
            padding: 0.2rem 0.5rem;
            border-radius: 0.3rem;
            margin-left: 0.5rem;
            border: 1px dotted #059669;
        }
        
        @media (max-width: 768px) {
            .hero-content h1 {
                font-size: 2rem;
            }
            
            .stats {
                grid-template-columns: repeat(2, 1fr);
                gap: 1rem;
            }
            
            .navbar {
                flex-wrap: wrap;
                gap: 0.5rem;
            }
            
            .navbar .nav-links {
                gap: 1rem;
            }
        }
    </style>
</head>
<body>
    <div class="confetti" style="left: 10%; animation-duration: 5s;"></div>
    <div class="confetti" style="left: 30%; animation-duration: 4s; background: var(--accent-color);"></div>
    <div class="confetti" style="left: 50%; animation-duration: 6s; background: var(--secondary-color);"></div>
    <div class="confetti" style="left: 70%; animation-duration: 4.5s;"></div>
    <div class="confetti" style="left: 90%; animation-duration: 5.5s; background: var(--accent-color);"></div>
    
    <nav class="navbar">
        <div class="logo">🤪 测试页面</div>
        <div class="nav-links">
            <a href="#home">🏠 首页</a>
            <a href="#features">✨ 功能</a>
            <a href="#gallery">🎨 画廊</a>
            <a href="#joke">😂 笑话</a>
        </div>
        <div class="actions">
            <a href="#login" class="btn btn-secondary">😜 登录</a>
            <a href="#register" class="btn btn-primary">🎉 注册</a>
        </div>
    </nav>
    
    <section class="hero" id="home">
        <div class="hero-content">
            <h1>欢迎来到测试页面（AI生成）！<span class="demo-label">纯属娱乐</span></h1>
            <p class="subtitle">🎮 这里是一个充满欢乐和惊喜的测试页面！<br>不要太认真，开心最重要！😄</p>
            <div class="actions">
                <a href="#joke" class="btn btn-primary">🎲 讲个笑话</a>
                <a href="#gallery" class="btn btn-secondary">🖼️ 看表情包</a>
            </div>
            <div class="stats">
                <div class="stat-item">
                    <div class="number">100%</div>
                    <div class="label">快乐指数</div>
                </div>
                <div class="stat-item">
                    <div class="number">999+</div>
                    <div class="label">表情包库存</div>
                </div>
                <div class="stat-item">
                    <div class="number">∞</div>
                    <div class="label">脑洞大小</div>
                </div>
                <div class="stat-item">
                    <div class="number">0</div>
                    <div class="label">正经程度</div>
                </div>
            </div>
        </div>
    </section>
    
    <section class="features" id="features">
        <div class="section-header">
            <h2>✨ 神奇功能介绍</h2>
            <p>这些功能可能没什么用，但看起来很酷！</p>
        </div>
        <div class="features-grid">
            <div class="feature-card">
                <div class="icon">🌈</div>
                <h3>彩虹生成器</h3>
                <p>点击就能看到彩虹！虽然没什么用，但心情会变好！</p>
            </div>
            <div class="feature-card">
                <div class="icon">🤖</div>
                <h3>AI聊天机器人</h3>
                <p>它会讲冷笑话，还会假装听懂你的话！</p>
            </div>
            <div class="feature-card">
                <div class="icon">🎵</div>
                <h3>音乐播放器</h3>
                <p>其实不会播放音乐，但按钮会动！</p>
            </div>
            <div class="feature-card">
                <div class="icon">🐱</div>
                <h3>猫咪图片生成</h3>
                <p>点击生成随机猫咪表情！喵~</p>
            </div>
            <div class="feature-card">
                <div class="icon">🎯</div>
                <h3>打地鼠游戏</h3>
                <p>经典游戏，但是地鼠会对你做鬼脸！</p>
            </div>
            <div class="feature-card">
                <div class="icon">🔮</div>
                <h3>未来预测器</h3>
                <p>输入问题，得到一个完全不准的答案！</p>
            </div>
        </div>
    </section>
    
    <section class="gallery" id="gallery">
        <div class="section-header">
            <h2>🎨 表情画廊</h2>
            <p>点击它们看看会发生什么！</p>
        </div>
        <div class="gallery-grid">
            <div class="gallery-item" title="点击！">😀</div>
            <div class="gallery-item" title="再点击！">😂</div>
            <div class="gallery-item" title="继续点击！">🤣</div>
            <div class="gallery-item" title="别停！">😎</div>
            <div class="gallery-item" title="快了！">🥳</div>
            <div class="gallery-item" title="马上！">🎉</div>
            <div class="gallery-item" title="就是现在！">🎊</div>
            <div class="gallery-item" title="你真棒！">👏</div>
            <div class="gallery-item" title="奖励！">🍕</div>
        </div>
    </section>
    
    <section class="joke-section" id="joke">
        <div class="section-header">
            <h2>😂 今日笑话</h2>
            <p>点击按钮获取新笑话！</p>
        </div>
        <div class="joke-card">
            <div class="emoji">🤣</div>
            <p class="joke">问：程序员为什么总是分不清万圣节和圣诞节？<br>答：因为 Oct 31 == Dec 25！</p>
            <button class="btn" onclick="changeJoke()">🔄 换一个笑话</button>
        </div>
    </section>
    
    <footer class="footer">
        <div class="footer-links">
            <a href="#home">🏠 首页</a>
            <a href="#features">✨ 功能</a>
            <a href="#gallery">🎨 画廊</a>
            <a href="#joke">😂 笑话</a>
        </div>
        <p>🎉 欢乐测试中心 © 2024 - 让测试变得有趣！</p>
        <p style="font-size: 0.8rem; margin-top: 0.5rem;">P.S. 这个页面没有任何实际功能，纯属娱乐！😜</p>
    </footer>
    
    <script>
        const jokes = [
            "问：程序员和产品经理打架谁赢？\n答：产品经理，因为程序员只会\"动手\"而不会\"动手\"！💪",
            "问：程序员最怕听到什么？\n答：\"这个需求很简单，就改一行代码\"！�",
            "问：为什么程序员讨厌Windows？\n答：因为你永远不知道它下一秒会不会自动更新！🔄",
            "问：为什么程序员喜欢骑电动车？\n答：因为他们受够了\"控制不住\"的东西！�",
            "问：为什么程序员的代码总是很烂？\n答：因为\"烂代码\"是祖传的！👴",
            "问：程序员最羡慕什么职业？\n答：牙医！因为他们说\"张嘴\"就真的只是张嘴！🦷",
            "问：为什么程序员喜欢喝咖啡？\n答：因为咖啡让他们保持\"运行中\"状态！☕",
            "问：程序员怎么离婚的？\n答：法院判决：你们的婚姻存在\"内存泄漏\"！⚖️",
            "问：程序员和苍蝇有什么共同点？\n答：都喜欢在\"代码\"上爬来爬去！🪰",
            "问：为什么程序员喜欢用Linux？\n答：因为Windows总是弹窗说\"系统即将关机\"！�",
            "问：程序员临死前说的最后一句话是什么？\n答：\"等我debug完这个bug就睡...\"！�",
            "问：程序员的遗言是什么？\n答：\"我的代码还在内存里没保存！\"！�",
            "问：为什么程序员害怕PM？\n答：因为PM有一种神奇的力量，能把\"一行代码\"变成\"一个项目\"！�",
            "问：为什么程序员的代码不需要注释？\n答：因为他们自己也不知道为什么要这样写！📝"
        ];
        
        let currentJokeIndex = 0;
        
        function changeJoke() {
            const jokeElement = document.querySelector('.joke');
            const emojiElement = document.querySelector('.emoji');
            const emojis = ['🤣', '😂', '😆', '😝', '😜', '🤪', '🙃', '😹'];
            
            jokeElement.textContent = jokes[currentJokeIndex];
            emojiElement.textContent = emojis[currentJokeIndex % emojis.length];
            currentJokeIndex = (currentJokeIndex + 1) % jokes.length;
        }
        
        const galleryItems = document.querySelectorAll('.gallery-item');
        const reactions = ['😍', '🥰', '😘', '😱', '🤯', '😎', '🤩', '😇', '🤗'];
        
        galleryItems.forEach(item => {
            item.addEventListener('click', () => {
                const randomReaction = reactions[Math.floor(Math.random() * reactions.length)];
                item.textContent = randomReaction;
                
                setTimeout(() => {
                    const originalEmojis = ['😀', '😂', '🤣', '😎', '🥳', '🎉', '🎊', '👏', '🍕'];
                    const index = Array.from(galleryItems).indexOf(item);
                    item.textContent = originalEmojis[index];
                }, 1000);
            });
        });
    </script>
</body>
</html>